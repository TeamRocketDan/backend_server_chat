package com.example.teamrocket.error.exception;

import com.example.teamrocket.error.type.ChatRoomErrorCode;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomException extends RuntimeException {
    private ChatRoomErrorCode errorCode;
    private String errorMessage;

    public ChatRoomException(ChatRoomErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getMessage();
    }
}
