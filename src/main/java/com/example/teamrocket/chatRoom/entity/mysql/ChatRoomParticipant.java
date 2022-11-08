package com.example.teamrocket.chatRoom.entity.mysql;

import lombok.*;

import com.example.teamrocket.config.jpa.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_participant")
public class ChatRoomParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private ChatRoomMySql chatRoomMySql;

    private Long userId;
    private boolean isOwner;
    private LocalDateTime leftAt;

    public void setLeftAt(LocalDateTime leftAt){
        this.leftAt = leftAt;
    }
}
