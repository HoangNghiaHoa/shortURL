package com.urlshortener.config;

import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public RedisClient redisClient() {
        String redisUri = redisPassword == null || redisPassword.isBlank()
                ? String.format("redis://%s:%d", redisHost, redisPort)
                : String.format("redis://:%s@%s:%d", redisPassword, redisHost, redisPort);
        return RedisClient.create(redisUri);
    }
}