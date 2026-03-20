package com.urlshortener.repository;

import com.urlshortener.entity.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho ClickAnalytics
 * Xử lý các thao tác với analytics data
 */
@Repository
public interface ClickAnalyticsRepository extends JpaRepository<ClickAnalytics, Long> {

    /**
     * Tìm tất cả analytics của một short code
     */
    List<ClickAnalytics> findByShortCode(String shortCode);

    /**
     * Lấy 100 clicks gần nhất của một short code
     */
    List<ClickAnalytics> findTop100ByShortCodeOrderByTimestampDesc(String shortCode);

    /**
     * Đếm tổng số clicks của một short code
     */
    @Query("SELECT COUNT(c) FROM ClickAnalytics c WHERE c.shortCode = :shortCode")
    Long countByShortCode(@Param("shortCode") String shortCode);

    /**
     * Thống kê clicks theo quốc gia
     * Returns: List<Object[]> với [country, count]
     */
    @Query("SELECT c.country, COUNT(c) as count FROM ClickAnalytics c " +
            "WHERE c.shortCode = :shortCode AND c.country IS NOT NULL " +
            "GROUP BY c.country ORDER BY count DESC")
    List<Object[]> getCountryStatsByShortCode(@Param("shortCode") String shortCode);

    /**
     * Thống kê clicks theo browser
     * Returns: List<Object[]> với [browser, count]
     */
    @Query("SELECT c.browser, COUNT(c) as count FROM ClickAnalytics c " +
            "WHERE c.shortCode = :shortCode AND c.browser IS NOT NULL " +
            "GROUP BY c.browser ORDER BY count DESC")
    List<Object[]> getBrowserStatsByShortCode(@Param("shortCode") String shortCode);

    /**
     * Thống kê clicks theo device type
     * Returns: List<Object[]> với [deviceType, count]
     */
    @Query("SELECT c.deviceType, COUNT(c) as count FROM ClickAnalytics c " +
            "WHERE c.shortCode = :shortCode AND c.deviceType IS NOT NULL " +
            "GROUP BY c.deviceType ORDER BY count DESC")
    List<Object[]> getDeviceStatsByShortCode(@Param("shortCode") String shortCode);

    /**
     * Thống kê clicks theo OS
     */
    @Query("SELECT c.operatingSystem, COUNT(c) as count FROM ClickAnalytics c " +
            "WHERE c.shortCode = :shortCode AND c.operatingSystem IS NOT NULL " +
            "GROUP BY c.operatingSystem ORDER BY count DESC")
    List<Object[]> getOSStatsByShortCode(@Param("shortCode") String shortCode);

    /**
     * Tìm clicks trong khoảng thời gian
     */
    @Query("SELECT c FROM ClickAnalytics c WHERE c.shortCode = :shortCode " +
            "AND c.timestamp BETWEEN :startTime AND :endTime ORDER BY c.timestamp DESC")
    List<ClickAnalytics> findByShortCodeAndTimeRange(
            @Param("shortCode") String shortCode,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime
    );

    /**
     * Đếm unique visitors (dựa trên IP)
     */
    @Query("SELECT COUNT(DISTINCT c.ipAddress) FROM ClickAnalytics c " +
            "WHERE c.shortCode = :shortCode")
    Long countUniqueVisitorsByShortCode(@Param("shortCode") String shortCode);

    /**
     * Thống kê clicks theo ngày (30 ngày gần nhất)
     * Returns: List<Object[]> với [date, clicks]
     */
    @Query(value = "SELECT DATE(to_timestamp(timestamp / 1000)) as date, COUNT(*) as clicks " +
            "FROM click_analytics WHERE short_code = :shortCode " +
            "GROUP BY DATE(to_timestamp(timestamp / 1000)) " +
            "ORDER BY date DESC LIMIT 30",
            nativeQuery = true)
    List<Object[]> getDailyClicksByShortCode(@Param("shortCode") String shortCode);

    /**
     * Thống kê clicks theo giờ trong ngày (24 giờ gần nhất)
     */
    @Query(value = "SELECT EXTRACT(HOUR FROM to_timestamp(timestamp / 1000)) as hour, " +
            "COUNT(*) as clicks FROM click_analytics " +
            "WHERE short_code = :shortCode " +
            "AND timestamp > :startTime " +
            "GROUP BY EXTRACT(HOUR FROM to_timestamp(timestamp / 1000)) " +
            "ORDER BY hour",
            nativeQuery = true)
    List<Object[]> getHourlyClicksByShortCode(
            @Param("shortCode") String shortCode,
            @Param("startTime") Long startTime
    );

    /**
     * Top cities theo số clicks
     */
    @Query("SELECT c.city, COUNT(c) as count FROM ClickAnalytics c " +
            "WHERE c.shortCode = :shortCode AND c.city IS NOT NULL " +
            "GROUP BY c.city ORDER BY count DESC")
    List<Object[]> getTopCitiesByShortCode(@Param("shortCode") String shortCode);

    /**
     * Lấy clicks từ một IP cụ thể
     */
    @Query("SELECT c FROM ClickAnalytics c WHERE c.shortCode = :shortCode " +
            "AND c.ipAddress = :ipAddress ORDER BY c.timestamp DESC")
    List<ClickAnalytics> findByShortCodeAndIpAddress(
            @Param("shortCode") String shortCode,
            @Param("ipAddress") String ipAddress
    );

    /**
     * Đếm clicks theo referer domain
     */
    @Query(value = "SELECT " +
            "CASE " +
            "  WHEN referer IS NULL THEN 'Direct' " +
            "  ELSE SUBSTRING(referer FROM 'https?://([^/]+)') " +
            "END as referer_domain, " +
            "COUNT(*) as count " +
            "FROM click_analytics " +
            "WHERE short_code = :shortCode " +
            "GROUP BY referer_domain " +
            "ORDER BY count DESC " +
            "LIMIT 20",
            nativeQuery = true)
    List<Object[]> getTopReferersByShortCode(@Param("shortCode") String shortCode);

    /**
     * Thống kê clicks theo ngôn ngữ
     */
    @Query("SELECT c.language, COUNT(c) as count FROM ClickAnalytics c " +
            "WHERE c.shortCode = :shortCode AND c.language IS NOT NULL " +
            "GROUP BY c.language ORDER BY count DESC")
    List<Object[]> getLanguageStatsByShortCode(@Param("shortCode") String shortCode);

    /**
     * Lấy tất cả analytics trong khoảng thời gian cho batch processing
     */
    @Query("SELECT c FROM ClickAnalytics c WHERE c.timestamp BETWEEN :startTime AND :endTime")
    List<ClickAnalytics> findByTimestampBetween(
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime
    );

    /**
     * Xóa analytics data cũ (retention policy)
     */
    @Query(value = "DELETE FROM click_analytics WHERE timestamp < :cutoffTime",
            nativeQuery = true)
    void deleteOldAnalytics(@Param("cutoffTime") Long cutoffTime);
}