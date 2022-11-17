package com.example.teamrocket.chatRoom.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("dayOfMessages")
public class DayOfMessages {
    @MongoId //ID => ChatRoom.getChatRoomId() +"#"+날짜기준
    private String id;

    @DocumentReference
    private List<Message> messages;
    private int messagesCount;

    public void setMessagesCount(int messagesCount){
        this.messagesCount = messagesCount;
    }
}
