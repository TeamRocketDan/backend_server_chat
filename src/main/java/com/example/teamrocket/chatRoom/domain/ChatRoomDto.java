package com.example.teamrocket.chatRoom.domain;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {

    private String id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private int curParticipant;
    private int maxParticipant;
    private boolean privateRoom;

    private String ownerNickName;
    private String ownerProfileImage;

    private String rcate1;
    private String rcate2;

    private String longitude;
    private String latitude;

    public static ChatRoomDto of(ChatRoomMySql chatRoom){
        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .title(chatRoom.getTitle())
                .startDate(chatRoom.getStartDate())
                .endDate(chatRoom.getEndDate())
                .maxParticipant(chatRoom.getMaxParticipant())
                .privateRoom(chatRoom.isPrivateRoom())
                .rcate1(chatRoom.getRcate1())
                .rcate2(chatRoom.getRcate2())
                .longitude(chatRoom.getLongitude())
                .latitude(chatRoom.getLatitude())
                .build();
    }

    public void setCurParticipant (int curParticipant) {
        this.curParticipant = curParticipant;
    }

    public void setOwnerInfo(String ownerNickName, String ownerProfileImage){
        this.ownerNickName = ownerNickName;
        this.ownerProfileImage = ownerProfileImage;
    }
}
