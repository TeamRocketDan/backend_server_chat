package com.example.teamrocket.chatRoom.repository.mongo;

import com.example.teamrocket.chatRoom.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends MongoRepository<Message,String> {
    Page<Message> findAllByRoomId(String roomId, Pageable pageable);
    Page<Message> findAllByRoomIdAndCreatedAtAfter(String roomId, LocalDateTime time, Pageable pageable);
}
