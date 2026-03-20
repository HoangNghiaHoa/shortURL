package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO cho request tạo short URL
 */
public class CreateURLRequest {

    @NotBlank(message = "URL cannot be empty")
    @Size(max = 2048, message = "URL too long")
    @Pattern(
            regexp = "^https?://.*",
            message = "URL must start with http:// or https://"
    )
    private String url;

    private Long userId;

    // Custom short code (optional)
    @Pattern(
            regexp = "^[a-zA-Z0-9]{6}$|^$",
            message = "Custom code must be exactly 6 alphanumeric characters"
    )
    private String customCode;

    // Expiration time in hours (optional)
    private Integer expiresInHours;

    // Password protection (optional)
    private String password;

    // Max clicks limit (optional)
    private Integer maxClicks;

    // Constructors
    public CreateURLRequest() {
    }

    public CreateURLRequest(String url, Long userId) {
        this.url = url;
        this.userId = userId;
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCustomCode() {
        return customCode;
    }

    public void setCustomCode(String customCode) {
        this.customCode = customCode;
    }

    public Integer getExpiresInHours() {
        return expiresInHours;
    }

    public void setExpiresInHours(Integer expiresInHours) {
        this.expiresInHours = expiresInHours;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getMaxClicks() {
        return maxClicks;
    }

    public void setMaxClicks(Integer maxClicks) {
        this.maxClicks = maxClicks;
    }
}