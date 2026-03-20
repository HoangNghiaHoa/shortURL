package com.urlshortener.service;

import com.urlshortener.entity.URLEntity;
import com.urlshortener.repository.URLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
    }
}