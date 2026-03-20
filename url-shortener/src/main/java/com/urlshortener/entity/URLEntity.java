package com.urlshortener.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity cho bảng URLs
 * Lưu trữ thông tin về các short URLs đã tạo
 *
 * @author URL Shortener Team
 * @version 1.0
 */
@Getter
@Entity
@Table(name = "urls",
        indexes = {
                @Index(name = "idx_urls_short_code", columnList = "short_code", unique = true),
                @Index(name = "idx_urls_user_id", columnList = "user_id"),
                @Index(name = "idx_urls_created_at", columnList = "created_at"),
                @Index(name = "idx_urls_original_url", columnList = "original_url")
        }
)
public class URLEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== Primary Key ====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ==================== Core Fields ====================

    /**
     * Mã rút gọn 6 ký tự (a-zA-Z0-9)
     * Ví dụ: "Xs2dF3"
     */
    @Column(name = "short_code", unique = true, nullable = false, length = 6)
    private String shortCode;

    /**
     * URL gốc cần rút gọn
     * Độ dài tối đa: 2048 ký tự
     */
    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    // ==================== User & Ownership ====================

    /**
     * ID của user tạo short URL này
     * NULL nếu là anonymous user
     */
    @Column(name = "user_id")
    private Long userId;

    // ==================== Timestamps ====================

    /**
     * Thời gian tạo (Unix timestamp milliseconds)
     */
    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    /**
     * Thời gian hết hạn (Unix timestamp milliseconds)
     * NULL = không hết hạn
     */
    @Column(name = "expires_at")
    private Long expiresAt;

    /**
     * Thời gian cập nhật cuối cùng
     */
    @Column(name = "updated_at")
    private Long updatedAt;

    // ==================== Statistics ====================

    /**
     * Tổng số lượt click
     * Update bởi trigger trong database
     */
    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;

    /**
     * Lượt click duy nhất (unique visitors)
     * Tính dựa trên IP address
     */
    @Column(name = "unique_clicks")
    private Long uniqueClicks = 0L;

    /**
     * Thời gian click cuối cùng
     */
    @Column(name = "last_clicked_at")
    private Long lastClickedAt;

    // ==================== Status & Settings ====================

    /**
     * Trạng thái active của URL
     * false = disabled, không thể redirect
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * URL có được public không
     * false = private, chỉ owner mới thấy stats
     */
    @Column(name = "is_public")
    private Boolean isPublic = true;

    // ==================== Security Features ====================

    /**
     * Password bảo vệ (hashed)
     * NULL = không có password
     */
    @Column(name = "password")
    private String password;

    /**
     * Số lượt click tối đa
     * NULL = không giới hạn
     */
    @Column(name = "max_clicks")
    private Integer maxClicks;

    // ==================== Custom Settings ====================

    /**
     * Tiêu đề custom cho URL (dùng cho preview)
     */
    @Column(name = "title", length = 200)
    private String title;

    /**
     * Mô tả custom
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Tags/labels (JSON array hoặc comma-separated)
     */
    @Column(name = "tags", length = 500)
    private String tags;

    /**
     * Metadata bổ sung (JSON format)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // ==================== Constructors ====================

    /**
     * Default constructor (required by JPA)
     */
    public URLEntity() {
    }

    /**
     * Constructor with essential fields
     */
    public URLEntity(String shortCode, String originalUrl, Long userId) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.userId = userId;
        this.createdAt = System.currentTimeMillis();
        this.clickCount = 0L;
        this.uniqueClicks = 0L;
        this.isActive = true;
        this.isPublic = true;
    }

    // ==================== Helper Methods ====================

    /**
     * Kiểm tra URL có hết hạn không
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return System.currentTimeMillis() > expiresAt;
    }

    /**
     * Kiểm tra có đạt giới hạn click chưa
     */
    public boolean hasReachedMaxClicks() {
        if (maxClicks == null) {
            return false;
        }
        return clickCount >= maxClicks;
    }

    /**
     * Kiểm tra URL có thể sử dụng không
     */
    public boolean isUsable() {
        return isActive && !isExpired() && !hasReachedMaxClicks();
    }

    /**
     * Kiểm tra có password protection không
     */
    public boolean isPasswordProtected() {
        return password != null && !password.isEmpty();
    }

    /**
     * Increment click count (manual)
     */
    public void incrementClickCount() {
        this.clickCount++;
        this.lastClickedAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Disable URL
     */
    public void disable() {
        this.isActive = false;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Enable URL
     */
    public void enable() {
        this.isActive = true;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Set expiration (hours from now)
     */
    public void setExpirationHours(int hours) {
        this.expiresAt = System.currentTimeMillis() + (hours * 3600000L);
    }

    /**
     * Get age in days
     */
    public long getAgeInDays() {
        long now = System.currentTimeMillis();
        long ageMillis = now - createdAt;
        return ageMillis / (24 * 3600000L);
    }

    /**
     * Get formatted created date
     */
    public String getFormattedCreatedAt() {
        return Instant.ofEpochMilli(createdAt).toString();
    }

    /**
     * Get full short URL
     */
    public String getFullShortUrl(String domain) {
        return domain + "/" + shortCode;
    }

    // ==================== Lifecycle Callbacks ====================

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = System.currentTimeMillis();
        }
        if (clickCount == null) {
            clickCount = 0L;
        }
        if (uniqueClicks == null) {
            uniqueClicks = 0L;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isPublic == null) {
            isPublic = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }

    // ==================== Getters and Setters ====================

    public void setId(Long id) {
        this.id = id;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    public void setUniqueClicks(Long uniqueClicks) {
        this.uniqueClicks = uniqueClicks;
    }

    public void setLastClickedAt(Long lastClickedAt) {
        this.lastClickedAt = lastClickedAt;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMaxClicks(Integer maxClicks) {
        this.maxClicks = maxClicks;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    // ==================== equals & hashCode ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URLEntity urlEntity = (URLEntity) o;
        return Objects.equals(id, urlEntity.id) &&
                Objects.equals(shortCode, urlEntity.shortCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shortCode);
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        return "URLEntity{" +
                "id=" + id +
                ", shortCode='" + shortCode + '\'' +
                ", originalUrl='" + originalUrl + '\'' +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", clickCount=" + clickCount +
                ", uniqueClicks=" + uniqueClicks +
                ", isActive=" + isActive +
                ", isPublic=" + isPublic +
                ", hasPassword=" + isPasswordProtected() +
                ", maxClicks=" + maxClicks +
                '}';
    }
}