package com.shailyverma.feasto.auth_users.dtos;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {

    private Long id;

    private String token;

    private List<String> roles;
}