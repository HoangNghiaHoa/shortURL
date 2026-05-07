package com.urlshortener.service;

import com.urlshortener.entity.URLEntity;
import com.urlshortener.repository.URLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SyncService {

    @Autowired
    private URLRepository urlRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String COUNTER_PREFIX = "counter:";

    /**
     * Chạy mỗi 2 phút (120000ms) để đồng bộ số click
     */
    @Scheduled(fixedRate = 120000)
    public void syncRedisToDb() {
        System.out.println(">>> Đang bắt đầu đồng bộ Click từ Redis về Database...");

        // 1. Lấy tất cả link đang có trong DB
        List<URLEntity> allUrls = urlRepository.findAll();

        for (URLEntity url : allUrls) {
            String shortCode = url.getShortCode();
            String redisKey = COUNTER_PREFIX + shortCode;

            // 2. Lấy số click hiện tại trong Redis
            String countStr = redisTemplate.opsForValue().get(redisKey);

            if (countStr != null) {
                Long redisCount = Long.parseLong(countStr);

                // 3. Nếu số trong Redis lớn hơn số trong DB thì mới cập nhật
                if (redisCount > url.getClickCount()) {
                    url.setClickCount(redisCount);
                    urlRepository.save(url);
                    System.out.println("Cập nhật link [" + shortCode + "]: " + redisCount + " clicks");
                }
            }
        }
        System.out.println(">>> Hoàn thành đồng bộ dữ liệu.");
    }
}