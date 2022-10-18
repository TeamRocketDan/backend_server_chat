package com.example.teamrocket.chatRoom.repository.mysql;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMySqlRepository extends JpaRepository<ChatRoomMySql,String> {
}
