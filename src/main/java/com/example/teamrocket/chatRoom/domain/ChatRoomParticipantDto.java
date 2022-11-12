package com.example.teamrocket.chatRoom.domain;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomParticipantDto {
    private Long userId;
    private boolean isOwner;
    private String nickname;
    private String profileImage;

    public static ChatRoomParticipantDto of(ChatRoomParticipant participant){
        User user = participant.getUser();

        return ChatRoomParticipantDto.builder()
                .userId(user.getId())
                .isOwner(participant.isOwner())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .build();
    }
}
