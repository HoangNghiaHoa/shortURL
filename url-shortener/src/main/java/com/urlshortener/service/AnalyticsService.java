package com.urlshortener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.AnalyticsData;
import com.urlshortener.entity.ClickAnalytics;
import com.urlshortener.repository.ClickAnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AnalyticsService {

    private static final String KAFKA_TOPIC = "url-clicks";
    private static final String STATS_PREFIX = "stats:";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ClickAnalyticsRepository clickAnalyticsRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private GeoIPService geoIPService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Ghi nhận click - publish to Kafka (async)
     */
    public void recordClick(AnalyticsData data) {

        System.out.println(">>> RECORD CLICK: " + data.getShortCode());
        try {
            // Enrich data với geo location
            enrichWithGeoData(data);

            // ✅ TRACK UNIQUE VISITOR (THIẾU)
            trackUniqueVisitor(data.getShortCode(), data.getIpAddress());
            // Publish to Kafka
            String jsonData = objectMapper.writeValueAsString(data);
            kafkaTemplate.send(KAFKA_TOPIC, data.getShortCode(), jsonData);

            // Update real-time stats in Redis
            updateRealtimeStats(data);

        } catch (Exception e) {
            // Log error nhưng không block redirect
            System.err.println("Failed to record analytics: " + e.getMessage());
        }
    }

    /**
     * Kafka consumer - xử lý và lưu vào DB
     */
    @KafkaListener(topics = KAFKA_TOPIC, groupId = "analytics-consumer")
    public void consumeClickEvent(String message) {

        System.out.println(">>> KAFKA CONSUME: " + message);
        try {
            AnalyticsData data = objectMapper.readValue(message, AnalyticsData.class);

            // Lưu vào PostgreSQL
            ClickAnalytics analytics = new ClickAnalytics();
            analytics.setShortCode(data.getShortCode());
            analytics.setTimestamp(data.getTimestamp());
            analytics.setIpAddress(data.getIpAddress());
            analytics.setCountry(data.getCountry());
            analytics.setCity(data.getCity());
            analytics.setRegion(data.getRegion());
            analytics.setLatitude(data.getLatitude());
            analytics.setLongitude(data.getLongitude());
            analytics.setBrowser(data.getBrowser());
            analytics.setOperatingSystem(data.getOperatingSystem());
            analytics.setDeviceType(data.getDeviceType());
            analytics.setUserAgent(data.getUserAgent());
            analytics.setReferer(data.getReferer());
            analytics.setLanguage(data.getAcceptLanguage());

            clickAnalyticsRepository.save(analytics);

        } catch (Exception e) {
            System.err.println("Failed to process click event: " + e.getMessage());
        }
    }

    /**
     * Enrich data với thông tin địa lý từ IP
     */
    private void enrichWithGeoData(AnalyticsData data) {
        if (data.getCountry() != null) {
            return;
        }

        Map<String, Object> geoData = geoIPService.getGeoData(data.getIpAddress());
        if (geoData == null) {
            return;
        }

        data.setCountry((String) geoData.get("country"));
        data.setCity((String) geoData.get("city"));
        data.setRegion((String) geoData.get("region"));
        data.setTimezone((String) geoData.get("timezone"));

        // latitude – convert an toàn
        Object lat = geoData.get("latitude");
        if (lat instanceof Double) {
            data.setLatitude(BigDecimal.valueOf((Double) lat));
        }

        // longitude – convert an toàn
        Object lon = geoData.get("longitude");
        if (lon instanceof Double) {
            data.setLongitude(BigDecimal.valueOf((Double) lon));
        }
    }

    /**
     * Update real-time stats trong Redis
     */
    private void updateRealtimeStats(AnalyticsData data) {
        String key = STATS_PREFIX + data.getShortCode();

        // Increment total clicks
        redisTemplate.opsForHash().increment(key, "total_clicks", 1);

        // Increment by country
        if (data.getCountry() != null) {
            redisTemplate.opsForHash().increment(
                    key + ":country",
                    data.getCountry(),
                    1
            );
        }

        // Increment by browser
        if (data.getBrowser() != null) {
            redisTemplate.opsForHash().increment(
                    key + ":browser",
                    data.getBrowser(),
                    1
            );
        }

        // Increment by device
        if (data.getDeviceType() != null) {
            redisTemplate.opsForHash().increment(
                    key + ":device",
                    data.getDeviceType(),
                    1
            );
        }

        // Increment by hour
        String hour = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(data.getTimestamp()));

        redisTemplate.opsForHash().increment(
                key + ":hourly",
                hour,
                1
        );

        // Set TTL 30 days
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
        redisTemplate.expire(key + ":country", 30, TimeUnit.DAYS);
        redisTemplate.expire(key + ":browser", 30, TimeUnit.DAYS);
        redisTemplate.expire(key + ":device", 30, TimeUnit.DAYS);
        redisTemplate.expire(key + ":hourly", 30, TimeUnit.DAYS);
    }

    /**
     * Lấy thống kê tổng hợp
     */
    public Map<String, Object> getStatistics(String shortCode) {
        Map<String, Object> stats = new HashMap<>();
        String key = STATS_PREFIX + shortCode;

        // Total clicks from Redis (real-time)
        Object totalClicks = redisTemplate.opsForHash().get(key, "total_clicks");
        stats.put("totalClicks", totalClicks != null ? Long.parseLong(totalClicks.toString()) : 0);

        // Stats by country
        Map<Object, Object> countryStats = redisTemplate.opsForHash().entries(key + ":country");
        stats.put("byCountry", convertToSortedList(countryStats));

        // Stats by browser
        Map<Object, Object> browserStats = redisTemplate.opsForHash().entries(key + ":browser");
        stats.put("byBrowser", convertToSortedList(browserStats));

        // Stats by device
        Map<Object, Object> deviceStats = redisTemplate.opsForHash().entries(key + ":device");
        stats.put("byDevice", convertToSortedList(deviceStats));

        // Hourly stats (last 24 hours)
        Map<Object, Object> hourlyStats = redisTemplate.opsForHash().entries(key + ":hourly");
        stats.put("hourlyTrend", hourlyStats);

        // Recent clicks from DB (last 100)
        List<ClickAnalytics> recentClicks = clickAnalyticsRepository
                .findTop100ByShortCodeOrderByTimestampDesc(shortCode);
        stats.put("recentClicks", recentClicks);

        // Unique visitors (approximate using HyperLogLog)
        String uniqueKey = key + ":unique";
        Long uniqueVisitors = redisTemplate.opsForHyperLogLog().size(uniqueKey);
        stats.put("uniqueVisitors", uniqueVisitors != null ? uniqueVisitors : 0);

        return stats;
    }

    /**
     * Convert map to sorted list
     */
    private List<Map<String, Object>> convertToSortedList(Map<Object, Object> map) {
        List<Map<String, Object>> result = new ArrayList<>();

        map.forEach((key, value) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", key.toString());
            item.put("count", Long.parseLong(value.toString()));
            result.add(item);
        });

        result.sort((a, b) ->
                Long.compare((Long) b.get("count"), (Long) a.get("count"))
        );

        return result;
    }

    /**
     * Track unique visitor
     */
    public void trackUniqueVisitor(String shortCode, String ipAddress) {
        String uniqueKey = STATS_PREFIX + shortCode + ":unique";
        redisTemplate.opsForHyperLogLog().add(uniqueKey, ipAddress);
        redisTemplate.expire(uniqueKey, 30, TimeUnit.DAYS);
    }
}