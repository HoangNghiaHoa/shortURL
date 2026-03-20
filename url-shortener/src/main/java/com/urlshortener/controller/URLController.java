package com.urlshortener.controller;

import com.urlshortener.dto.CreateURLRequest;
import com.urlshortener.dto.AnalyticsData;
import com.urlshortener.service.URLShortenerService;
import com.urlshortener.service.AnalyticsService;
import com.urlshortener.security.RateLimiter;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "http://localhost:5173")
public class URLController {

    @Autowired
    private URLShortenerService urlShortenerService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private RateLimiter rateLimiter;

    /**
     * API tạo link rút gọn với rate limiting
     */
    @PostMapping("/api/shorten")
    public ResponseEntity<?> createShortURL(
            @RequestBody CreateURLRequest request,
            HttpServletRequest httpRequest) {

        String clientIP = getClientIP(httpRequest);

        // Rate limiting: 100 requests/hour per IP
        Bucket bucket = rateLimiter.resolveBucket(clientIP);
        if (!bucket.tryConsume(1)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Too many requests. Please try again later.");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
        }

        try {
            // Validate URL
            if (!isValidURL(request.getUrl())) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Invalid URL format")
                );
            }

            String shortCode = urlShortenerService.createShortURL(
                    request.getUrl(),
                    request.getUserId()
            );

            Map<String, String> response = new HashMap<>();
            response.put("shortCode", shortCode);
            response.put("shortURL", "https://yourdomain.com/" + shortCode);
            response.put("originalURL", request.getUrl());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create short URL"));
        }
    }

    /**
     * Redirect endpoint - xử lý chuyển hướng và thu thập analytics
     */
    @GetMapping("/{shortCode}")
    public void redirect(
            @PathVariable String shortCode,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        System.out.println(">>> REDIRECT HIT: " + shortCode);
        // Validate short code format
        if (!isValidShortCode(shortCode)) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid short code");
            return;
        }

        // Rate limiting cho redirect: 1000 requests/minute per IP
        String clientIP = getClientIP(request);
        Bucket bucket = rateLimiter.resolveRedirectBucket(clientIP);

        if (!bucket.tryConsume(1)) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests");
            return;
        }

        // Lấy URL gốc
        String originalURL = urlShortenerService.getOriginalURL(shortCode);

        if (originalURL == null) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "URL not found");
            return;
        }

        // Thu thập analytics data (async)
        AnalyticsData analyticsData = extractAnalyticsData(request, shortCode);
        analyticsService.recordClick(analyticsData);
        analyticsService.trackUniqueVisitor(analyticsData.getShortCode(), analyticsData.getIpAddress());



        // Redirect với 301 (Permanent) hoặc 302 (Temporary)
        response.setStatus(HttpStatus.FOUND.value());
        response.setHeader("Location", originalURL);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    }

    /**
     * API lấy thống kê
     */
    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<?> getStats(@PathVariable String shortCode) {
        try {
            Map<String, Object> stats = analyticsService.getStatistics(shortCode);

            if (stats.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve statistics"));
        }
    }

    /**
     * Trích xuất analytics data từ request
     */
    private AnalyticsData extractAnalyticsData(HttpServletRequest request, String shortCode) {
        AnalyticsData data = new AnalyticsData();

        data.setShortCode(shortCode);
        data.setTimestamp(System.currentTimeMillis());
        data.setIpAddress(getClientIP(request));
        data.setUserAgent(request.getHeader("User-Agent"));
        data.setReferer(request.getHeader("Referer"));

        // Trích xuất thông tin từ User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            data.setBrowser(extractBrowser(userAgent));
            data.setOperatingSystem(extractOS(userAgent));
            data.setDeviceType(extractDeviceType(userAgent));
        }

        // Headers bổ sung
        data.setAcceptLanguage(request.getHeader("Accept-Language"));
        data.setAcceptEncoding(request.getHeader("Accept-Encoding"));

        // CloudFlare headers (nếu dùng CF)
        data.setCountry(request.getHeader("CF-IPCountry"));
        data.setCity(request.getHeader("CF-IPCity"));

        return data;
    }

    /**
     * Lấy IP thực của client (xử lý proxy/CDN)
     */
    private String getClientIP(HttpServletRequest request) {
        String[] headerNames = {
                "CF-Connecting-IP",      // CloudFlare
                "X-Forwarded-For",       // Standard proxy
                "X-Real-IP",             // Nginx
                "X-Client-IP",
                "True-Client-IP"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For có thể chứa nhiều IP
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    private boolean isValidURL(String url) {
        return url != null && url.matches("^https?://.*");
    }

    private boolean isValidShortCode(String code) {
        return code != null && code.matches("^[a-zA-Z0-9]{6}$");
    }

    private String extractBrowser(String userAgent) {
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        if (userAgent.contains("Opera")) return "Opera";
        return "Other";
    }

    private String extractOS(String userAgent) {
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS")) return "MacOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iOS")) return "iOS";
        return "Other";
    }

    private String extractDeviceType(String userAgent) {
        if (userAgent.contains("Mobile")) return "Mobile";
        if (userAgent.contains("Tablet")) return "Tablet";
        return "Desktop";
    }
}