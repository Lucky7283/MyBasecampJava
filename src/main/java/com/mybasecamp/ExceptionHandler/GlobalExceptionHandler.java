package com.mybasecamp.ExceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<Map<String,Object>> handleAlreadyExistException(AlreadyExistException ex) {
        Map<String,Object> map = new HashMap<>();
        map.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(map);
    }
}
