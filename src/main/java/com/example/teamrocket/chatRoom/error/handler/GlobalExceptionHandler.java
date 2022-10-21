package com.example.teamrocket.chatRoom.error.handler;

import com.example.teamrocket.chatRoom.error.exception.ChatRoomException;
import com.example.teamrocket.chatRoom.error.exception.UserException;
import com.example.teamrocket.chatRoom.error.result.GlobalErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
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
}
