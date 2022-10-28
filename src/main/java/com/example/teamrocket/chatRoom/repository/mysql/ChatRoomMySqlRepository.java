package com.example.teamrocket.chatRoom.repository.mysql;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMySqlRepository extends JpaRepository<ChatRoomMySql,String> {
    Page<ChatRoomMySql> findAllByRcate1AndPrivateRoomFalseAndDeletedAtIsNullOrderByStartDate(String rcate1, Pageable pageable);
    Page<ChatRoomMySql> findAllByRcate1AndRcate2AndPrivateRoomFalseAndDeletedAtIsNullOrderByStartDate(String rcate1, String rcate2, Pageable pageable);
}
