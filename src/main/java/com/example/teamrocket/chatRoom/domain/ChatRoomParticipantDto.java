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
    private boolean isOwner;
    private String nickname;
    private String profileImage;

    public static ChatRoomParticipantDto of(ChatRoomParticipant participant){
        return ChatRoomParticipantDto.builder()
                .userId(participant.getUserId())
                .leftAt(participant.getLeftAt())
                .isOwner(participant.isOwner())
                .nickname(participant.getNickname())
                .profileImage(participant.getProfileImage())
                .build();
    }
}
