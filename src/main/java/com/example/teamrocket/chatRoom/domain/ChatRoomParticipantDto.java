package com.example.teamrocket.chatRoom.domain;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomParticipantDto {
    private Long userId;
    private LocalDateTime leftAt;

    public ChatRoomParticipantDto of(ChatRoomParticipant participant){
        return ChatRoomParticipantDto.builder()
                .userId(participant.getUserId())
                .leftAt(participant.getLeftAt())
                .build();
    }
}
