package com.urlshortener.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * GeoIP Service sử dụng MaxMind GeoIP2 Database
 * Download database: https://dev.maxmind.com/geoip/geoip2/geolite2/
 */
@Service
public class GeoIPService {

    private boolean isLocalIP(String ip) {
        return ip.equals("127.0.0.1")
                || ip.equals("::1")
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("172."); // ⭐ docker bridge
    }

    private DatabaseReader dbReader;

    @PostConstruct
    public void init() {
        try {
            // Đường dẫn đến GeoLite2-City.mmdb
            File database = new File("data/GeoLite2-City.mmdb");
            dbReader = new DatabaseReader.Builder(database).build();
        } catch (Exception e) {
            System.err.println("Failed to initialize GeoIP database: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin địa lý từ IP address
     */
    public Map<String, Object> getGeoData(String ipAddress) {
        Map<String, Object> geoData = new HashMap<>();

        // ✅ MOCK GEO DATA CHO LOCAL (DEV)
        if (isLocalIP(ipAddress)) {
            geoData.put("country", "Vietnam");
            geoData.put("countryName", "Vietnam");
            geoData.put("city", "Ho Chi Minh City");
            geoData.put("region", "HCM");
            geoData.put("continent", "Asia");
            geoData.put("latitude", 10.8231);
            geoData.put("longitude", 106.6297);
            geoData.put("timezone", "Asia/Ho_Chi_Minh");
            return geoData;
        }

        try {
            if (dbReader == null) {
                System.err.println("GeoIP DB not initialized");
                return geoData;
            }

            InetAddress ip = InetAddress.getByName(ipAddress);
            CityResponse response = dbReader.city(ip);


            Country country = response.getCountry();
            if (country != null) {
                geoData.put("country", country.getIsoCode());
                geoData.put("countryName", country.getName());
            }

            // City
            City city = response.getCity();
            if (city != null) {
                geoData.put("city", city.getName());
            }

            // Subdivision (State/Province)
            Subdivision subdivision = response.getMostSpecificSubdivision();
            if (subdivision != null) {
                geoData.put("region", subdivision.getName());
                geoData.put("regionCode", subdivision.getIsoCode());
            }

            // Location
            Location location = response.getLocation();
            if (location != null) {
                geoData.put("latitude",
                        location.getLatitude() != null ? location.getLatitude() : 0.0);
                geoData.put("longitude",
                        location.getLongitude() != null ? location.getLongitude() : 0.0);
                geoData.put("timezone", location.getTimeZone());
                geoData.put("accuracyRadius", location.getAccuracyRadius());
            }

            // Postal Code
            Postal postal = response.getPostal();
            if (postal != null) {
                geoData.put("postalCode", postal.getCode());
            }

            // Continent
            Continent continent = response.getContinent();
            if (continent != null) {
                geoData.put("continent", continent.getName());
                geoData.put("continentCode", continent.getCode());
            }

        } catch (Exception e) {
            System.err.println("GeoIP lookup failed for IP: " + ipAddress);
            // Return partial data or fallback
            geoData.put("country", "Unknown");
        }
        System.out.println("CLIENT IP = " + ipAddress);
        return geoData;
    }

    /**
     * Kiểm tra IP có phải từ VPN/Proxy không (optional)
     */
    public boolean isVPNorProxy(String ipAddress) {
        try {
            InetAddress ip = InetAddress.getByName(ipAddress);
            CityResponse response = dbReader.city(ip);

            // MaxMind cung cấp traits để detect VPN/Proxy
            // Cần GeoIP2 Precision hoặc GeoIP2 Anonymous IP database
            return false; // Placeholder

        } catch (Exception e) {
            return false;
        }
    }
}