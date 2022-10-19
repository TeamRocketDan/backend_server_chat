package com.example.teamrocket.chatRoom.repository.mysql;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant,Long> {
    Optional<ChatRoomParticipant> findByChatRoomMySqlAndUserId(ChatRoomMySql chatRoomMySql, Long UserId);
    List<ChatRoomParticipant> findAllByChatRoomMySql(ChatRoomMySql chatRoomMySql);
    void deleteAllByChatRoomMySql(ChatRoomMySql chatRoomMySql);
}