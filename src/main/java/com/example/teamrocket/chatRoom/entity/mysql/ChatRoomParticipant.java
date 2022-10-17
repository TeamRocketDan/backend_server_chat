package com.example.teamrocket.chatRoom.entity.mysql;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "chat_participant")
public class ChatRoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private ChatRoomMySql chatRoomMySql;

    private Long userId;
}
