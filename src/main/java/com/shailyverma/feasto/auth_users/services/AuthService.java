package com.shailyverma.feasto.auth_users.services;

import com.shailyverma.feasto.auth_users.dtos.LoginRequest;
import com.shailyverma.feasto.auth_users.dtos.LoginResponse;
import com.shailyverma.feasto.auth_users.dtos.RegistrationRequest;
import com.shailyverma.feasto.response.Response;

public interface
AuthService {
    Response<?> registration(RegistrationRequest registrationRequest);
    Response<LoginResponse> login(LoginRequest loginRequest);
}
