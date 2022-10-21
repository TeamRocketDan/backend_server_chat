package com.example.teamrocket.chatRoom.domain;


public class ChatRoomServiceResult {

   private String chatRoomId;
   private String userId;

    ChatRoomServiceResult(String chatRoomId, String userId){
        this.chatRoomId = chatRoomId;
        this.userId = userId;
    }
}
