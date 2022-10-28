package com.example.teamrocket.chatRoom.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private String imgPath;
    private String senderName;
    private String senderImgSrc;
    private String message;
    private LocalDateTime createdAt;

    public void setMessage(String message){
        this.message = message;
    }
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }
}
