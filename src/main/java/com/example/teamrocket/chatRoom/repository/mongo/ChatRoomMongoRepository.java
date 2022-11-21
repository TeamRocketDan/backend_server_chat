package com.example.teamrocket.chatRoom.repository.mongo;

import com.example.teamrocket.chatRoom.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ChatRoomMongoRepository extends MongoRepository<ChatRoom,String> {
}
