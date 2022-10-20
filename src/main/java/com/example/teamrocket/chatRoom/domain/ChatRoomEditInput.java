package com.example.teamrocket.chatRoom.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomEditInput {

    private String title;
    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private int maxParticipant;
    private boolean privateRoom;
    private String password;

}
