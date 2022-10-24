package com.example.teamrocket.chatRoom.domain;


public class ChatRoomServiceResult {

   private final String chatRoomId;
   private final Long userId;

    public ChatRoomServiceResult(String chatRoomId, Long userId){
        this.chatRoomId = chatRoomId;
        this.userId = userId;
    }
}
