package com.example.teamrocket.chatRoom.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RedisHash("message")
public class Message implements Serializable {
    private String senderName;
    private String senderImgSrc;
    private String message;
    private LocalDateTime createdAt;
}
