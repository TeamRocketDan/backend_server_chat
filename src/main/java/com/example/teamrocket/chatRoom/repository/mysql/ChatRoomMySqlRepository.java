package com.example.teamrocket.chatRoom.repository.mysql;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomMySqlRepository extends JpaRepository<ChatRoomMySql,String> {
    Page<ChatRoomMySql> findAllByRcate1AndRcate2AndDeletedAtIsNullOrderByStartDate(String rcate1, String rcate2, Pageable pageable);
    Optional<ChatRoomMySql> findByIdAndDeletedAtIsNull(String id);
    @Query(
            value = "SELECT * FROM chat c where c.end_date < ?1 limit 1000",
            nativeQuery = true
    )
    List<ChatRoomMySql> findExpiredDate(@Param("date") LocalDate date);

    @Modifying
    @Query("delete from ChatRoomMySql c where c in :list")
    void deleteAllByChatRoomList(@Param("list") List<ChatRoomMySql> list);

}
