package com.cmc.board.user;

public interface UserService {
    void createUser(UserRequest request);
    void deleteUser(String email);
}