package com.example.teamrocket.chatRoom.domain;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {

    private String title;
    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private int maxParticipant;
    private boolean privateRoom;

    private String rcate1;
    private String rcate2;
    private String rcate3;

    private String longitude;
    private String latitude;

    public static ChatRoomDto of(ChatRoomMySql chatRoom){
        return ChatRoomDto.builder()
                .title(chatRoom.getTitle())
                .start_date(chatRoom.getStart_date())
                .end_date(chatRoom.getEnd_date())
                .maxParticipant(chatRoom.getMaxParticipant())
                .privateRoom(chatRoom.isPrivateRoom())
                .rcate1(chatRoom.getRcate1())
                .rcate2(chatRoom.getRcate2())
                .rcate3(chatRoom.getRcate3())
                .longitude(chatRoom.getLongitude())
                .latitude(chatRoom.getLatitude())
                .build();
    }

    public static List<ChatRoomDto> of(List<ChatRoomMySql> chatRooms) {
        return chatRooms.stream().map(ChatRoomDto::of).collect(Collectors.toList());
    }
}
