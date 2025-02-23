package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthData {
    private String token;
    private UserInfo user;
} 