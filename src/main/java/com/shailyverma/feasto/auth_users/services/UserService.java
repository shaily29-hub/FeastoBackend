package com.shailyverma.feasto.auth_users.services;

import com.shailyverma.feasto.auth_users.dtos.UserDTO;
import com.shailyverma.feasto.auth_users.entity.User;
import com.shailyverma.feasto.response.Response;

import java.util.List;

public interface UserService {
    User getCurrentLoggedInUser();
    Response<List<UserDTO>> getAllUsers();
    Response<UserDTO> getOwnAccountDetails();
    Response<?> updateOwnAccount(UserDTO userDTO);
    Response<?> deactivateOwnAccount();
}
