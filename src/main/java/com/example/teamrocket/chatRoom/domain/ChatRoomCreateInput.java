package com.example.teamrocket.chatRoom.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomCreateInput {

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
