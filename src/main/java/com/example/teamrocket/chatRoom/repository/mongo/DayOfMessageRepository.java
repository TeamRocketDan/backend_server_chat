package com.example.teamrocket.chatRoom.repository.mongo;

import com.example.teamrocket.chatRoom.entity.DayOfMessages;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayOfMessageRepository extends MongoRepository<DayOfMessages,String> {
}
