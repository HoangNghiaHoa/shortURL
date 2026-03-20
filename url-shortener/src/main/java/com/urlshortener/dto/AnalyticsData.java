package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO chứa toàn bộ thông tin analytics từ mỗi lượt click
 * Serializable để có thể gửi qua Kafka
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsData implements Serializable {

    private static final long serialVersionUID = 1L;

    // Basic info
    @JsonProperty("short_code")
    private String shortCode;

    @JsonProperty("timestamp")
    private Long timestamp;

    // Network info
    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("user_agent")
    private String userAgent;

    @JsonProperty("referer")
    private String referer;

    // Geographic info (from GeoIP or CloudFlare)
    @JsonProperty("country")
    private String country;

    @JsonProperty("country_name")
    private String countryName;

    @JsonProperty("city")
    private String city;

    @JsonProperty("region")
    private String region;

    @JsonProperty("region_code")
    private String regionCode;

    @JsonProperty("latitude")
    private BigDecimal latitude;

    @JsonProperty("longitude")
    private BigDecimal longitude;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("continent")
    private String continent;

    @JsonProperty("continent_code")
    private String continentCode;

    // Device info (parsed from User-Agent)
    @JsonProperty("browser")
    private String browser;

    @JsonProperty("browser_version")
    private String browserVersion;

    @JsonProperty("operating_system")
    private String operatingSystem;

    @JsonProperty("os_version")
    private String osVersion;

    @JsonProperty("device_type")
    private String deviceType; // Desktop, Mobile, Tablet

    @JsonProperty("device_brand")
    private String deviceBrand; // Apple, Samsung, etc.

    @JsonProperty("device_model")
    private String deviceModel;

    // Additional HTTP headers
    @JsonProperty("accept_language")
    private String acceptLanguage;

    @JsonProperty("accept_encoding")
    private String acceptEncoding;

    // Session tracking
    @JsonProperty("session_id")
    private String sessionId;

    // ISP & Network
    @JsonProperty("isp")
    private String isp;

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("asn")
    private String asn; // Autonomous System Number

    // Security flags
    @JsonProperty("is_vpn")
    private Boolean isVpn;

    @JsonProperty("is_proxy")
    private Boolean isProxy;

    @JsonProperty("is_tor")
    private Boolean isTor;

    @JsonProperty("is_bot")
    private Boolean isBot;

    // UTM parameters (from referer)
    @JsonProperty("utm_source")
    private String utmSource;

    @JsonProperty("utm_medium")
    private String utmMedium;

    @JsonProperty("utm_campaign")
    private String utmCampaign;

    @JsonProperty("utm_term")
    private String utmTerm;

    @JsonProperty("utm_content")
    private String utmContent;

    // Screen info (if available from client-side JS)
    @JsonProperty("screen_width")
    private Integer screenWidth;

    @JsonProperty("screen_height")
    private Integer screenHeight;

    @JsonProperty("viewport_width")
    private Integer viewportWidth;

    @JsonProperty("viewport_height")
    private Integer viewportHeight;

    // Constructors
    public AnalyticsData() {
    }

    // Getters and Setters

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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
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

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
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

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getContinentCode() {
        return continentCode;
    }

    public void setContinentCode(String continentCode) {
        this.continentCode = continentCode;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public void setDeviceBrand(String deviceBrand) {
        this.deviceBrand = deviceBrand;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    public String getAcceptEncoding() {
        return acceptEncoding;
    }

    public void setAcceptEncoding(String acceptEncoding) {
        this.acceptEncoding = acceptEncoding;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getAsn() {
        return asn;
    }

    public void setAsn(String asn) {
        this.asn = asn;
    }

    public Boolean getIsVpn() {
        return isVpn;
    }

    public void setIsVpn(Boolean isVpn) {
        this.isVpn = isVpn;
    }

    public Boolean getIsProxy() {
        return isProxy;
    }

    public void setIsProxy(Boolean isProxy) {
        this.isProxy = isProxy;
    }

    public Boolean getIsTor() {
        return isTor;
    }

    public void setIsTor(Boolean isTor) {
        this.isTor = isTor;
    }

    public Boolean getIsBot() {
        return isBot;
    }

    public void setIsBot(Boolean isBot) {
        this.isBot = isBot;
    }

    public String getUtmSource() {
        return utmSource;
    }

    public void setUtmSource(String utmSource) {
        this.utmSource = utmSource;
    }

    public String getUtmMedium() {
        return utmMedium;
    }

    public void setUtmMedium(String utmMedium) {
        this.utmMedium = utmMedium;
    }

    public String getUtmCampaign() {
        return utmCampaign;
    }

    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }

    public String getUtmTerm() {
        return utmTerm;
    }

    public void setUtmTerm(String utmTerm) {
        this.utmTerm = utmTerm;
    }

    public String getUtmContent() {
        return utmContent;
    }

    public void setUtmContent(String utmContent) {
        this.utmContent = utmContent;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }

    public Integer getViewportWidth() {
        return viewportWidth;
    }

    public void setViewportWidth(Integer viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    public Integer getViewportHeight() {
        return viewportHeight;
    }

    public void setViewportHeight(Integer viewportHeight) {
        this.viewportHeight = viewportHeight;
    }

    @Override
    public String toString() {
        return "AnalyticsData{" +
                "shortCode='" + shortCode + '\'' +
                ", timestamp=" + timestamp +
                ", ipAddress='" + ipAddress + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", browser='" + browser + '\'' +
                ", os='" + operatingSystem + '\'' +
                ", deviceType='" + deviceType + '\'' +
                '}';
    }
}