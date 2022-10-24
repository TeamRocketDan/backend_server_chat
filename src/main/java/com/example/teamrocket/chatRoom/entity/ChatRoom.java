package com.example.teamrocket.chatRoom.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.*;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Column;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@RedisHash("chatRoom")
@Document("chatRoom")
public class ChatRoom implements Serializable {
    @MongoId
    @Field(targetType = FieldType.OBJECT_ID)
    private String chatRoomId;

    @DocumentReference
    private List<DayOfMessages> dayOfMessages;

    private String chatRoomIdGenerate(String roomId){
        String dayOfMessageStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return roomId+"#"+dayOfMessageStr;
    }
}
