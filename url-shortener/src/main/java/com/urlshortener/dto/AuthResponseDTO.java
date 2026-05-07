package com.urlshortener.dto;

public class AuthResponseDTO {
    private Long userId;
    private String email;
    private String fullName;
    private String token;

    public AuthResponseDTO(Long userId, String email, String fullName, String token) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.token = token;
    }

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getToken() { return token; }
}