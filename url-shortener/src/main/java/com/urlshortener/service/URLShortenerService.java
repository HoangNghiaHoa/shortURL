package com.urlshortener.service;

import com.urlshortener.entity.URLEntity;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.URLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class URLShortenerService {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_CODE_LENGTH = 6;
    private static final String URL_PREFIX = "url:";
    private static final String COUNTER_PREFIX = "counter:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private URLRepository urlRepository;

    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private ClickAnalyticsRepository clickAnalyticsRepository;

    private final SecureRandom random = new SecureRandom();

    /**
     * Tạo mã rút gọn 6 ký tự với cơ chế retry để tránh trùng lặp
     */
    @Transactional
    public String createShortURL(String originalURL, Long userId) {
        // Kiểm tra URL đã tồn tại trong DB (tùy chọn)
        URLEntity existing = urlRepository.findByOriginalUrl(originalURL);
        if (existing != null && !existing.isExpired()) {
            return existing.getShortCode();
        }

        String shortCode = generateUniqueShortCode();

        // Lưu vào database
        URLEntity urlEntity = new URLEntity();
        urlEntity.setShortCode(shortCode);
        urlEntity.setOriginalUrl(originalURL);
        urlEntity.setUserId(userId);
        urlEntity.setCreatedAt(System.currentTimeMillis());
        urlEntity.setClickCount(0L);
        urlRepository.save(urlEntity);

        // Cache vào Redis với TTL 24h (có thể điều chỉnh)
        String redisKey = URL_PREFIX + shortCode;
        redisTemplate.opsForValue().set(redisKey, originalURL, 24, TimeUnit.HOURS);

        // Khởi tạo counter cho analytics
        redisTemplate.opsForValue().set(COUNTER_PREFIX + shortCode, "0");

        return shortCode;
    }

    /**
     * Lấy URL gốc từ mã rút gọn với multi-layer caching
     */
    public String getOriginalURL(String shortCode) {
        // Layer 1: Redis Cache (cực nhanh - <1ms)
        String redisKey = URL_PREFIX + shortCode;
        String cachedURL = redisTemplate.opsForValue().get(redisKey);

        if (cachedURL != null) {
            incrementCounter(shortCode);
            return cachedURL;
        }

        // Layer 2: Database lookup
        URLEntity urlEntity = urlRepository.findByShortCode(shortCode);

        if (urlEntity == null || urlEntity.isExpired()) {
            return null;
        }

        // Update cache
        redisTemplate.opsForValue().set(redisKey, urlEntity.getOriginalUrl(), 24, TimeUnit.HOURS);

        incrementCounter(shortCode);
        return urlEntity.getOriginalUrl();
    }
    /** Lấy all link của 1 user */
    // Trong URLShortenerService.java
    public List<URLEntity> getLinksByUserId(Long userId) {
        List<URLEntity> links = urlRepository.findByUserIdOrderByCreatedAtDesc(userId);
        links.forEach(link -> {
            String redisCount = redisTemplate.opsForValue().get("counter:" + link.getShortCode());
            if (redisCount != null) {
                link.setClickCount(Long.parseLong(redisCount));
            }
        });
        return links;
    }
    /** Get Statistics by user*/
    public Map<String, Object> getTotalStatsByUserId(Long userId) {
        // 1. Lấy tất cả link của User này
        List<URLEntity> userLinks = urlRepository.findByUserId(userId);

        long totalClicks = 0;
        Map<String, Long> browserStats = new HashMap<>();
        Map<String, Long> countryStats = new HashMap<>();
        Map<String, Long> deviceStats = new HashMap<>();

        // 2. Duyệt qua từng link để gom số liệu từ Redis
        for (URLEntity url : userLinks) {
            String code = url.getShortCode();

            // Cộng tổng click
            String clicks = redisTemplate.opsForValue().get("counter:" + code);
            if (clicks != null) totalClicks += Long.parseLong(clicks);

            // Gom dữ liệu Browser (từ Redis Hash)
            Map<Object, Object> browsers = redisTemplate.opsForHash().entries("stats:" + code + ":browser");
            browsers.forEach((k, v) -> browserStats.merge((String) k, Long.parseLong((String) v), Long::sum));

            // Gom dữ liệu Country
            Map<Object, Object> countries = redisTemplate.opsForHash().entries("stats:" + code + ":country");
            countries.forEach((k, v) -> countryStats.merge((String) k, Long.parseLong((String) v), Long::sum));
        }

        // 3. Đóng gói kết quả trả về giống hệt cấu trúc của 1 link để FE không bị lỗi
        Map<String, Object> result = new HashMap<>();
        result.put("totalClicks", totalClicks);
        result.put("byBrowser", convertMapToList(browserStats));
        result.put("byCountry", convertMapToList(countryStats));

        return result;
    }

    // Hàm phụ để biến Map thành List Object cho đúng format FE (ví dụ: {name: 'Chrome', count: 10})
    private List<Map<String, Object>> convertMapToList(Map<String, Long> map) {
        return map.entrySet().stream().map(e -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", e.getKey());
            item.put("count", e.getValue());
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Sinh mã ngẫu nhiên 6 ký tự với collision detection
     */
    private String generateUniqueShortCode() {
        int maxRetries = 5;

        for (int i = 0; i < maxRetries; i++) {
            String shortCode = generateRandomCode();

            // Kiểm tra trong Redis trước (nhanh hơn DB)
            String redisKey = URL_PREFIX + shortCode;
            Boolean exists = redisTemplate.hasKey(redisKey);

            if (exists == null || !exists) {
                // Double check trong DB
                if (urlRepository.findByShortCode(shortCode) == null) {
                    return shortCode;
                }
            }
        }

        // Fallback: sử dụng timestamp-based code
        return generateTimestampBasedCode();
    }

    /**
     * Sinh mã ngẫu nhiên thuần túy
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    /**
     * Sinh mã dựa trên timestamp khi collision cao
     */
    private String generateTimestampBasedCode() {
        long timestamp = System.currentTimeMillis();
        StringBuilder code = new StringBuilder();

        // Chuyển timestamp sang base62
        while (timestamp > 0 && code.length() < SHORT_CODE_LENGTH) {
            int index = (int) (timestamp % CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
            timestamp /= CHARACTERS.length();
        }

        // Thêm random nếu thiếu
        while (code.length() < SHORT_CODE_LENGTH) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return code.toString();
    }

    /**
     * Tăng counter trong Redis (atomic operation)
     */
    private void incrementCounter(String shortCode) {
        String counterKey = COUNTER_PREFIX + shortCode;
        redisTemplate.opsForValue().increment(counterKey);
    }

    /**
     * Lấy số lượt click
     */
    public Long getClickCount(String shortCode) {
        String counterKey = COUNTER_PREFIX + shortCode;
        String count = redisTemplate.opsForValue().get(counterKey);
        return count != null ? Long.parseLong(count) : 0L;
    }/**
     * Customer Alias
     */
    @Transactional
    public String updateCustomAlias(String oldShortCode, String newAlias, Long userId) {
        // 1. Kiểm tra sở hữu và tồn tại
        URLEntity url = urlRepository.findByShortCode(oldShortCode);
        if (url == null || !url.getUserId().equals(userId)) {
            throw new RuntimeException("URL không tồn tại hoặc bạn không có quyền");
        }

        // 2. Kiểm tra bí danh mới đã có ai dùng chưa
        if (urlRepository.existsByShortCode(newAlias)) {
            throw new RuntimeException("Bí danh này đã được sử dụng");
        }

        // 3. Cập nhật DATABASE (Cả bảng URL và bảng ClickAnalytics)
        url.setShortCode(newAlias);
        urlRepository.saveAndFlush(url);
        clickAnalyticsRepository.updateShortCodeForClicks(oldShortCode, newAlias);

        // 4. Cập nhật REDIS (Đây là phần quan trọng nhất)
        migrateRedisData(oldShortCode, newAlias);

        return newAlias;
    }

    /**
     * Di chuyển toàn bộ dữ liệu thống kê từ Key cũ sang Key mới trong Redis
     */
    private void migrateRedisData(String oldCode, String newCode) {
        // Danh sách các loại Key em đang dùng trong AnalyticsService
        String[] keyTemplates = {
                "url:%s",           // Cache URL gốc
                "counter:%s",       // Tổng số click (String)
                "stats:%s",         // Hash tổng hợp
                "stats:%s:country",  // Hash quốc gia
                "stats:%s:browser",  // Hash trình duyệt
                "stats:%s:device",   // Hash thiết bị
                "stats:%s:hourly",   // Hash giờ
                "stats:%s:source",   // Hash nguồn (QR/Direct)
                "stats:%s:unique"    // HyperLogLog unique visitors
        };

        for (String template : keyTemplates) {
            String oldKey = String.format(template, oldCode);
            String newKey = String.format(template, newCode);

            // Nếu Key cũ tồn tại thì đổi tên sang Key mới
            if (Boolean.TRUE.equals(redisTemplate.hasKey(oldKey))) {
                redisTemplate.rename(oldKey, newKey);
            }
        }
    }
    /**Check ALias*/
    public boolean existsByShortCode(String shortCode) {
        return urlRepository.existsByShortCode(shortCode);
    }

 @Transactional
    public void updateQrConfig(String shortCode, String configJson) {
        // 1. Tìm URL (Repository đang trả về URLEntity trực tiếp)
        URLEntity url = urlRepository.findByShortCode(shortCode);

        // 2. Kiểm tra null thay vì dùng orElseThrow
        if (url == null) {
            throw new RuntimeException("Không tìm thấy mã URL: " + shortCode);
        }

        // 3. Cập nhật và lưu
        url.setQrConfig(configJson);
        urlRepository.save(url);
    }


}