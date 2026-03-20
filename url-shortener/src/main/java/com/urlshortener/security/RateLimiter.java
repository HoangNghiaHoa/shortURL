package com.urlshortener.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Rate Limiter sử dụng Bucket4j với Redis backend
 * Phân tán và shared state giữa các instances
 */
@Component
public class RateLimiter {

    private final ProxyManager<String> proxyManager;

    @Autowired
    public RateLimiter(RedisClient redisClient) {
        StatefulRedisConnection<String, byte[]> connection =
                redisClient.connect(io.lettuce.core.codec.RedisCodec.of(
                        io.lettuce.core.codec.StringCodec.UTF8,
                        io.lettuce.core.codec.ByteArrayCodec.INSTANCE
                ));

        this.proxyManager = LettuceBasedProxyManager.builderFor(connection)
                .build();
    }

    /**
     * Rate limit cho API tạo short URL: 100 requests/hour
     */
    public Bucket resolveBucket(String key) {
        Supplier<BucketConfiguration> configSupplier = getCreateURLConfiguration();
        return proxyManager.builder().build(key, configSupplier);
    }

    /**
     * Rate limit cho redirect: 1000 requests/minute
     */
    public Bucket resolveRedirectBucket(String key) {
        Supplier<BucketConfiguration> configSupplier = getRedirectConfiguration();
        return proxyManager.builder().build("redirect:" + key, configSupplier);
    }

    /**
     * Configuration cho API tạo URL
     * - 100 requests per hour
     * - Burst: 10 requests
     */
    private Supplier<BucketConfiguration> getCreateURLConfiguration() {
        return () -> {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(100)
                    .refillIntervally(100, Duration.ofHours(1))
                    .build();

            Bandwidth burst = Bandwidth.builder()
                    .capacity(10)
                    .refillIntervally(10, Duration.ofMinutes(1))
                    .build();

            return BucketConfiguration.builder()
                    .addLimit(limit)
                    .addLimit(burst)
                    .build();
        };
    }

    /**
     * Configuration cho Redirect
     * - 1000 requests per minute
     * - Burst: 50 requests per 10 seconds
     */
    private Supplier<BucketConfiguration> getRedirectConfiguration() {
        return () -> {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(1000)
                    .refillIntervally(1000, Duration.ofMinutes(1))
                    .build();

            Bandwidth burst = Bandwidth.builder()
                    .capacity(50)
                    .refillIntervally(50, Duration.ofSeconds(10))
                    .build();

            return BucketConfiguration.builder()
                    .addLimit(limit)
                    .addLimit(burst)
                    .build();
        };
    }
}