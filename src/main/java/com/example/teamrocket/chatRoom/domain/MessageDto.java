package com.example.teamrocket.chatRoom.domain;

import com.example.teamrocket.chatRoom.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    private String senderName;
    private String senderImgSrc;
    private String message;
    private LocalDateTime createdAt;

    public static MessageDto of(Message message){
        return MessageDto.builder()
                .senderName(message.getSenderName())
                .senderImgSrc(message.getSenderImgSrc())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt()).build();
    }
}
