package com.example.teamrocket.chatRoom.repository;

import com.example.teamrocket.chatRoom.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRoomMongoRepository extends MongoRepository<ChatRoom,String> {
}
