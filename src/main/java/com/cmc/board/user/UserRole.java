package com.cmc.board.user;

import lombok.Getter;

public enum UserRole {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    @Getter
    private String role;

    UserRole(String role) {
        this.role = role;
    }
}
