package com.example.teamrocket.chatRoom.entity.mysql;

import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomEditInput;
import com.example.teamrocket.config.jpa.BaseEntity;
import com.example.teamrocket.user.entity.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
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
    private User owner;

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

    public static ChatRoomMySql of(User user, ChatRoomCreateInput input){
        return ChatRoomMySql.builder()
                .owner(user)
                .chatRoomStatus(ChatRoomStatus.PRE_TRAVEL)
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
                .build();
    }

    public void update (ChatRoomEditInput param){

        this.title = param.getTitle();
        this.start_date = param.getStart_date();
        this.end_date = param.getEnd_date();
        this.maxParticipant = param.getMaxParticipant();
        this.privateRoom = param.isPrivateRoom();
        this.password = param.getPassword();
    }
    public void setId(String id){
        this.id = id;
    }

    public void delete(){
        this.deletedAt = LocalDateTime.now();
    }
}
