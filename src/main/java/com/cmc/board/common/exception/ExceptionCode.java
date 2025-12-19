package com.cmc.board.common.exception;

import lombok.Getter;

public enum ExceptionCode {
    USER_NOT_FOUND(404, "User not found"),
    POST_NOT_FOUND(404, "Post not found"),
    COMMENT_NOT_FOUND(404, "Comment not found"),
    CATEGORY_NOT_FOUND(404, "Category not found"),
    BOOKMARK_NOT_FOUND(404, "Bookmark not found"),

    EMAIL_DUPLICATION(409, "Email already exists"),   // 이메일 중복
    NICKNAME_DUPLICATION(409, "Nickname already exists"), // 닉네임 중복
    CATEGORY_DUPLICATION(409, "Category already exists"), // 닉네임 중복

    INPUT_CANNOT_BE_NULL(400, "Input value cannot be null"),
    CATEGORY_CANNOT_BE_DELETED(400, "Category cannot be deleted"),

    NOT_AUTHORIZED(403, "Not authorized to access this resource");

    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
