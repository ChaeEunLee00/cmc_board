package com.cmc.board.user.domain;

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
