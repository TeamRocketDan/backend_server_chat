package com.example.teamrocket.chatRoom.repository.mysql;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant,Long> {
    Optional<ChatRoomParticipant> findByChatRoomMySqlAndUserId(ChatRoomMySql chatRoomMySql, Long UserId);
    Page<ChatRoomParticipant> findAllByUserId(Long userId, Pageable pageable);
    void deleteAllByChatRoomMySql(ChatRoomMySql chatRoomMySql);
    @Query("select c from ChatRoomParticipant c where c.chatRoomMySql in :list")
    List<ChatRoomParticipant> findAllByChatRoomMySqlExpired(@Param("list") List<ChatRoomMySql> list);

    List<ChatRoomParticipant> findAllByChatRoomMySql(ChatRoomMySql chatRoomMySql);

    @Modifying
    @Query("delete from ChatRoomParticipant c where c in :list")
    void deleteAllByChatRoomParticipants(@Param("list") List<ChatRoomParticipant> list);
}
