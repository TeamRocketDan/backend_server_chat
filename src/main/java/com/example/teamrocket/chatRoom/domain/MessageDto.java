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
    private Long userId;
    private String message;
    private String profileImage;
    private String senderName;
    private LocalDateTime createdAt;

    public static MessageDto of(Message message){
        return MessageDto.builder()
                .userId(message.getUserId())
                .profileImage(message.getProfileImage())
                .senderName(message.getSenderName())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt()).build();
    }
}
