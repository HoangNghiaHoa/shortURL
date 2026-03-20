package com.urlshortener.repository;

import com.urlshortener.entity.URLEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho URLEntity
 * Xử lý các thao tác CRUD với bảng urls
 */
@Repository
public interface URLRepository extends JpaRepository<URLEntity, Long> {

    /**
     * Tìm URL entity theo short code
     */
    URLEntity findByShortCode(String shortCode);

    /**
     * Tìm URL entity theo original URL
     * Dùng để check xem URL đã được rút gọn chưa
     */
    URLEntity findByOriginalUrl(String originalUrl);

    /**
     * Tìm tất cả URLs của một user
     */
    List<URLEntity> findByUserId(Long userId);

    /**
     * Tìm URLs active của user, sắp xếp theo thời gian tạo
     */
    @Query("SELECT u FROM URLEntity u WHERE u.userId = :userId AND u.isActive = true ORDER BY u.createdAt DESC")
    List<URLEntity> findActiveUrlsByUserId(@Param("userId") Long userId);

    /**
     * Đếm số lượng URLs của một user
     */
    @Query("SELECT COUNT(u) FROM URLEntity u WHERE u.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * Tìm các URLs đã hết hạn
     */
    @Query("SELECT u FROM URLEntity u WHERE u.expiresAt < :currentTime AND u.isActive = true")
    List<URLEntity> findExpiredUrls(@Param("currentTime") Long currentTime);

    /**
     * Tìm URLs có click count cao nhất
     */
    @Query("SELECT u FROM URLEntity u WHERE u.isActive = true ORDER BY u.clickCount DESC")
    List<URLEntity> findTopClickedUrls();

    /**
     * Tìm URLs được tạo trong khoảng thời gian
     */
    @Query("SELECT u FROM URLEntity u WHERE u.createdAt BETWEEN :startTime AND :endTime ORDER BY u.createdAt DESC")
    List<URLEntity> findByCreatedAtBetween(
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime
    );

    /**
     * Kiểm tra short code có tồn tại không
     */
    boolean existsByShortCode(String shortCode);
}