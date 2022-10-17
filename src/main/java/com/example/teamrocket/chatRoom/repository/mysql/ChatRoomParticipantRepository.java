package com.example.teamrocket.chatRoom.repository.mysql;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant,Long> {
}
