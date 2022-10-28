package com.example.teamrocket.chatRoom.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RedisHash("message")
@Document("message")
public class Message implements Serializable {

    @MongoId
    private String id;

    private String roomId;
    private String senderName;
    private String senderImgSrc;
    private String message;
    private LocalDateTime createdAt;

    public void updateRoomIdAndCreatedAt(String id){
        this.roomId = roomId;
        this.createdAt = LocalDateTime.now();
    }
    public void updateMessage(String message){
        this.message = message;
    }
}
