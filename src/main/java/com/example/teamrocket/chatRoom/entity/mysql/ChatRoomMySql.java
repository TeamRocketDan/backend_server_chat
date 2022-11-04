package com.example.teamrocket.chatRoom.entity.mysql;

import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomEditInput;
import com.example.teamrocket.config.jpa.BaseEntity;
import com.example.teamrocket.user.entity.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDate startDate;
    private LocalDate endDate;
    private int maxParticipant;


    private String rcate1;
    private String rcate2;

    private String longitude;
    private String latitude;

    private LocalDateTime deletedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chatRoomMySql")
    private List<ChatRoomParticipant> participants;

    public static ChatRoomMySql of(User user, ChatRoomCreateInput input){
        return ChatRoomMySql.builder()
                .owner(user)
                .chatRoomStatus(ChatRoomStatus.PRE_TRAVEL)
                .title(input.getTitle())
                .startDate(input.getStartDate())
                .endDate(input.getEndDate())
                .maxParticipant(input.getMaxParticipant())
                .rcate1(input.getRcate1())
                .rcate2(input.getRcate2())
                .longitude(input.getLongitude())
                .latitude(input.getLatitude())
                .build();
    }

    public void update (ChatRoomEditInput param){

        this.title = param.getTitle();
        this.startDate = param.getStartDate();
        this.endDate = param.getEndDate();
        this.maxParticipant = param.getMaxParticipant();
    }
    public void setId(String id){
        this.id = id;
    }

    public void delete(){
        this.deletedAt = LocalDateTime.now();
    }
}
