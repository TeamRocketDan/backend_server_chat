package com.example.teamrocket.chatRoom.domain;

import lombok.Getter;

@Getter
public class ChatRoomEnterResult {

    private final String chatRoomId;
    private final Long userId;
    private final boolean isNewUser;

    ChatRoomEnterResult(String chatRoomId, Long userId, boolean isNewUser){
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.isNewUser = isNewUser;
    }

}
