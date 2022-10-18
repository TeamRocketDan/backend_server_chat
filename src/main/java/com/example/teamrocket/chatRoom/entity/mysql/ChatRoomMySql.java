package com.example.teamrocket.chatRoom.entity.mysql;

import com.example.teamrocket.config.jpa.BaseEntity;
import com.example.teamrocket.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.repository.CountQuery;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat")
public class ChatRoomMySql extends BaseEntity {

    @Id @Column(name = "chat_id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;//chat owner

    @Enumerated(EnumType.STRING)
    private ChatRoomStatus chatRoomStatus;
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

    private LocalDateTime deletedAt;
}
