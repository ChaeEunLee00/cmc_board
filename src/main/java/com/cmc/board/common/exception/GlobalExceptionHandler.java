package com.cmc.board.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<String> handleBusiness(BusinessLogicException e) {
        return ResponseEntity.status(e.getExceptionCode().getStatus()).body(e.getExceptionCode().getMessage());
    }
}
