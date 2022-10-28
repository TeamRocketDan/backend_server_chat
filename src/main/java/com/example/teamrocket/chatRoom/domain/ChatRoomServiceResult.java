package com.example.teamrocket.chatRoom.domain;


import lombok.Getter;

@Getter
public class ChatRoomServiceResult {

   private final String chatRoomId;
   private final Long userId;

    public ChatRoomServiceResult(String chatRoomId, Long userId){
        this.chatRoomId = chatRoomId;
        this.userId = userId;
    }
}
