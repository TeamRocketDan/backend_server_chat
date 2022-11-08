package com.example.teamrocket.chatRoom.repository.mysql;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant,Long> {
    Optional<ChatRoomParticipant> findByChatRoomMySqlAndUserId(ChatRoomMySql chatRoomMySql, Long UserId);
    Page<ChatRoomParticipant> findAllByUserId(Long userId, Pageable pageable);
    void deleteAllByChatRoomMySql(ChatRoomMySql chatRoomMySql);
}
