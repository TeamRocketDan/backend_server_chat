package com.example.teamrocket.chatRoom.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.List;

@Document("chatRoom")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChatRoom implements Serializable {
    @MongoId
    @Column(name = "chat_room_id")
    private String chatRoomId;

    private List<Message> messages;
}
