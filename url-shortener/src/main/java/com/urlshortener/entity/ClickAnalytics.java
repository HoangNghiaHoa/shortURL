package com.urlshortener.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entity cho bảng Click Analytics
 * Lưu trữ toàn bộ thông tin về mỗi lượt click vào short URL
 */
@Entity
@Table(name = "click_analytics", indexes = {
        @Index(name = "idx_analytics_short_code", columnList = "short_code"),
        @Index(name = "idx_analytics_timestamp", columnList = "timestamp"),
        @Index(name = "idx_analytics_country", columnList = "country"),
        @Index(name = "idx_analytics_device_type", columnList = "device_type"),
        @Index(name = "idx_analytics_browser", columnList = "browser"),
        @Index(name = "idx_analytics_code_time", columnList = "short_code,timestamp")
})
public class ClickAnalytics implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false, length = 6)
    private String shortCode;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    // ==================== IP & Location ====================
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "timezone", length = 50)
    private String timezone;

    // ==================== Device Info ====================
    @Column(name = "browser", length = 50)
    private String browser;

    @Column(name = "operating_system", length = 50)
    private String operatingSystem;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    // ==================== Technical Info ====================
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "referer", columnDefinition = "TEXT")
    private String referer;

    @Column(name = "language", length = 100)
    private String language;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    // ==================== Constructors ====================
    public ClickAnalytics() {
    }

    public ClickAnalytics(String shortCode, Long timestamp) {
        this.shortCode = shortCode;
        this.timestamp = timestamp;
    }

    // ==================== Getters and Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    // ==================== toString ====================
    @Override
    public String toString() {
        return "ClickAnalytics{" +
                "id=" + id +
                ", shortCode='" + shortCode + '\'' +
                ", timestamp=" + timestamp +
                ", ipAddress='" + ipAddress + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", browser='" + browser + '\'' +
                ", os='" + operatingSystem + '\'' +
                ", deviceType='" + deviceType + '\'' +
                '}';
    }

    // ==================== equals & hashCode ====================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClickAnalytics that = (ClickAnalytics) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}