package com.example.teamrocket.chatRoom.error.exception;

import com.example.teamrocket.chatRoom.error.type.ChatRoomErrorCode;

public class ChatRoomException extends RuntimeException {
    private final ChatRoomErrorCode errorCode;
    private final String errorMessage;

    public ChatRoomException(ChatRoomErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getMessage();
    }
}
