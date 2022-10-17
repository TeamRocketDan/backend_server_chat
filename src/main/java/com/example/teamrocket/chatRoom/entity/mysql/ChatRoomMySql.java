package com.example.teamrocket.chatRoom.entity.mysql;

import com.example.teamrocket.chatRoom.domain.ChatRoomInput;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat")
public class ChatRoomMySql {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;
    private Long userId;//chat owner

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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    List<ChatRoomParticipant> participants;

    public static ChatRoomMySql of(Long userId, ChatRoomInput input){
        return ChatRoomMySql.builder()
                .userId(userId)
                .chatRoomStatus(ChatRoomStatus.ING)
                .title(input.getTitle())
                .start_date(input.getStart_date())
                .end_date(input.getEnd_date())
                .maxParticipant(input.getMaxParticipant())
                .privateRoom(input.isPrivateRoom())
                .password(input.getPassword()) //차후 수정 필요
                .rcate1(input.getRcate1())
                .rcate2(input.getRcate2())
                .rcate3(input.getRcate3())
                .longitude(input.getLongitude())
                .latitude(input.getLatitude())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
