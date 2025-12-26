package com.cmc.board.user.service;

import com.cmc.board.user.dto.UserRequest;

public interface UserService {
    void createUser(UserRequest request);
    void deleteUser(String email);
}