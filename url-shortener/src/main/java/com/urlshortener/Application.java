package com.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Application Class
 *
 * Features:
 * - URL Shortening with 6-character codes
 * - Multi-layer caching (Redis + Nginx)
 * - DDoS protection with rate limiting
 * - Comprehensive analytics with Kafka streaming
 * - GeoIP location tracking
 * - Horizontal scalability
 */
@SpringBootApplication
@EnableCaching
@EnableKafka
@EnableAsync
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        System.out.println("========================================");
        System.out.println("   URL Shortener Application Started   ");
        System.out.println("========================================");
        System.out.println("API Endpoints:");
        System.out.println("  - POST /api/shorten   : Create short URL");
        System.out.println("  - GET  /{shortCode}   : Redirect to original URL");
        System.out.println("  - GET  /api/stats/{shortCode} : Get statistics");
        System.out.println();
        System.out.println("Monitoring:");
        System.out.println("  - Health: http://localhost:8080/health");
        System.out.println("  - Metrics: http://localhost:8080/actuator/prometheus");
        System.out.println("  - Grafana: http://localhost:3000");
        System.out.println("========================================");
    }
}