package com.cmc.board.common.exception;

import lombok.Getter;

public enum ExceptionCode {
    USER_NOT_FOUND(404, "User not found"),
    EMAIL_DUPLICATION(409, "Email already exists"),   // 이메일 중복
    NICKNAME_DUPLICATION(409, "Nickname already exists"); // 닉네임 중복

    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
