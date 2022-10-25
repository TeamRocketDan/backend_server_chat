package com.example.teamrocket.chatRoom.repository.mysql;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMySqlRepository extends JpaRepository<ChatRoomMySql,String> {
    Page<ChatRoomMySql> findByRcate1OrderByStart_date(String rcate1, String start_date,Pageable pageable);
    Page<ChatRoomMySql> findByRcate1AndRcate2OrderByStart_date(String rcate1, String rcate2, Pageable pageable);
}
