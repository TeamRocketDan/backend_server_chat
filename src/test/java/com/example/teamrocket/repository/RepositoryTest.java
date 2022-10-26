package com.example.teamrocket.repository;


import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomMySqlRepository;
import com.example.teamrocket.service.ChatServiceImpl;
import com.example.teamrocket.user.entity.User;
import com.example.teamrocket.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
@Transactional
public class RepositoryTest {

    @Autowired
    private ChatRoomMySqlRepository chatRoomMySqlRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testingSave() throws Exception{
        User byId = userRepository.findById(1L).get();
        ChatRoomCreateInput input = ChatRoomCreateInput.builder()
                .title("채팅방1")
                .start_date(LocalDateTime.now())
                .end_date(LocalDateTime.now().minusDays(1))
                .maxParticipant(8)
                .privateRoom(false)
                .password("1234")
                .rcate1("rcate1")
                .rcate2("rcate2")
                .rcate3("rcate3")
                .longitude("위도")
                .latitude("경도")
                .build();

        ChatRoomMySql of = ChatRoomMySql.of(byId, input);
        of.setId("1234");
        ChatRoomMySql save = chatRoomMySqlRepository.save(of);
        Assertions.assertThat(save.getId()).isEqualTo("1234");
    }
}
