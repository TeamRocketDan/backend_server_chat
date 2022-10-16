package com.example.teamrocket.chatRoom.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.List;

@Document("chatRoom")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ChatRoom implements Serializable {
    @MongoId
    @Column(name = "chat_room_id")
    private String chatRoomId;

    private List<Message> messages;
}
