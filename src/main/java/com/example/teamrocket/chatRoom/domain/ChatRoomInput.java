package com.example.teamrocket.chatRoom.domain;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChatRoomInput {

    private String title;
    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private int maxParticipant;
    private boolean privateRoom;
    private String password;

    private String rcate1;
    private String rcate2;
    private String rcate3;

    private String longitude;
    private String latitude;

}
