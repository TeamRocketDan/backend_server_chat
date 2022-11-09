package com.example.teamrocket.chatRoom.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class RoomInfoDto {

    private final String roomTitle;
    private final List<ChatRoomParticipantDto> participants;

    public RoomInfoDto(String roomTitle, List<ChatRoomParticipantDto> participants){
        this.roomTitle = roomTitle;
        this.participants = participants;
    }
}
