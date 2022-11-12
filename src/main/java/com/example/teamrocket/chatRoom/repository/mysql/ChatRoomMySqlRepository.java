package com.example.teamrocket.chatRoom.repository.mysql;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChatRoomMySqlRepository extends JpaRepository<ChatRoomMySql,String> {
    Page<ChatRoomMySql> findAllByRcate1AndRcate2AndDeletedAtIsNullAndEndDateAfterOrderByStartDate(String rcate1, String rcate2, LocalDate date, Pageable pageable);
    Optional<ChatRoomMySql> findByIdAndDeletedAtIsNullAndEndDateAfter(String id,LocalDate date);

    @Query(value = "SELECT c FROM ChatRoomMySql c " +
            "join ChatRoomParticipant participant on (participant.chatRoomMySql.id=c.id and participant.user = :user ) " +
            "where c.deletedAt IS NULL AND c.endDate> :date " +
            "order by c.startDate")
    Page<ChatRoomMySql> findAllByUserIdAndAndDeletedAtIsNullAndEndDateAfterOrderByStartDate(User user, LocalDate date, Pageable pageable);

    @Query(
            value = "SELECT * FROM chat c where c.end_date < ?1 limit 1000",
            nativeQuery = true
    )
    List<ChatRoomMySql> findExpiredDate(@Param("date") LocalDate date);

    @Modifying
    @Query("delete from ChatRoomMySql c where c in :list")
    void deleteAllByChatRoomList(@Param("list") List<ChatRoomMySql> list);
}
