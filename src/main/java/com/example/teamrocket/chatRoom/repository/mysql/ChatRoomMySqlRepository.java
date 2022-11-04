package com.example.teamrocket.chatRoom.repository.mysql;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomMySqlRepository extends JpaRepository<ChatRoomMySql,String> {
    Page<ChatRoomMySql> findAllByRcate1AndRcate2AndDeletedAtIsNullOrderByStartDate(String rcate1, String rcate2, Pageable pageable);
    Optional<ChatRoomMySql> findByIdAndDeletedAtIsNull(String id);
}
