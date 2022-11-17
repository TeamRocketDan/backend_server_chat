package com.example.teamrocket.chatRoom.repository.mongo;

import com.example.teamrocket.chatRoom.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends MongoRepository<Message,String> {
    Page<Message> findAllByRoomIdAndCreatedAtBetween(String roomId, LocalDateTime createdAt, LocalDateTime atTime, PageRequest pageRequest);

    Long countByRoomIdAndCreatedAtBetween(String roomId, LocalDate targetDate, LocalDate plusDays);
}
