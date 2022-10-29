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

    private String title;
    private LocalDate start_date;
    private LocalDate end_date;
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
                .title(chatRoom.getTitle())
                .start_date(chatRoom.getStartDate())
                .end_date(chatRoom.getEndDate())
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
