package com.urlshortener.dto;

import lombok.Data;

@Data
public class UpdateAliasRequest {
    private String oldShortCode;
    private String newAlias;
    private Long userId;
}