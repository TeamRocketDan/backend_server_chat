package com.example.teamrocket.error.handler;

import com.example.teamrocket.error.exception.ChatRoomException;
import com.example.teamrocket.error.exception.UserException;
import com.example.teamrocket.error.result.GlobalErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e){
        log.error("Exception is occurred.", e);

        GlobalErrorResult result = GlobalErrorResult.of(e.getMessage());
        return new ResponseEntity<>(result, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<?> userExceptionHandler(UserException e){
        GlobalErrorResult result = GlobalErrorResult.of(e.getErrorMessage());
        return new ResponseEntity<>(result,e.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(ChatRoomException.class)
    public ResponseEntity<?> chatRoomExceptionHandler(ChatRoomException e){
        GlobalErrorResult result = GlobalErrorResult.of(e.getErrorMessage());
        return new ResponseEntity<>(result,e.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e){
        GlobalErrorResult result = GlobalErrorResult.of(e.getAllErrors().get(0).getDefaultMessage());
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
}
