package com.example.teamrocket.dbTest;

import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomStatus;
import com.example.teamrocket.chatRoom.repository.mongo.ChatRoomMongoRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
import com.example.teamrocket.user.entity.User;
import com.example.teamrocket.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
public class DbConnectionSaveTest {
    @Autowired
    private RedisTemplateRepository redisTemplateRepository;
    @Autowired
    private ChatRoomMongoRepository chatRoomMongoRepository;
    @Autowired
    private ChatRoomMySqlRepository chatRoomMySqlRepository;
    @Autowired
    private UserRepository userRepository;


    @BeforeEach // *주의 테스트를 위해 레디스 의 모든 데이터를 플러쉬 함
    void init() {
        redisTemplateRepository.flushAll();
    }

    @Test
    @DisplayName("레디스 커넥션 저장,가져오기 테스트")
    void saveRedisTest() throws Exception{
        String roomId = "roomIdTest";
        Message m1 = Message.builder()
                .senderName("로사")
                .message("우리가 누군지 물으신다면 나 로사")
                .createdAt(LocalDateTime.now())
                .build();
        redisTemplateRepository.saveToLeft(roomId,m1);

        Message m2 = Message.builder()
                .senderName("로이")
                .message("나 로이")
                .createdAt(LocalDateTime.now())
                .build();
        redisTemplateRepository.saveToLeft(roomId,m2);

        List<Message> range = redisTemplateRepository.getAllMessageByRoomId(roomId);

        assertThat(range.size()).isEqualTo(2);
        assertThat(range)
                .extracting(Message::getMessage)
                .containsExactly(m2.getMessage(),m1.getMessage());

        redisTemplateRepository.deleteMessageByRoomId(roomId);
    }

    @Test
    @DisplayName("몽고디비 저장,가져오기 테스트")
    void saveMongoDbTest(){
        String id = UUID.randomUUID().toString();
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomId(id)
                .build();
        ChatRoom save = chatRoomMongoRepository.save(chatRoom);
        ChatRoom findOne = chatRoomMongoRepository.findById(id).get();
        assertThat(save.getChatRoomId()).isEqualTo(findOne.getChatRoomId());

        chatRoomMongoRepository.delete(findOne);
    }

    @Test
    @DisplayName("MySql 저장,가져오기 테스트")
    void saveMysqlDbTest() throws Exception{
        User user = userRepository.findById(1L).get();

        String s = UUID.randomUUID().toString();
        ChatRoomMySql chatRoom = ChatRoomMySql.builder()
                .id(s)
                .title("test")
                .maxParticipant(10)
                .owner(user)
                .privateRoom(false)
                .password("0")
                .start_date(LocalDateTime.now())
                .end_date(LocalDateTime.now())
                .latitude("a")
                .longitude("b")
                .chatRoomStatus(ChatRoomStatus.TRAVEL)
                .rcate1("abc")
                .rcate2("cde")
                .rcate3("ghi")
                .build();

        ChatRoomMySql save = chatRoomMySqlRepository.save(chatRoom);


        ChatRoomMySql findChatRoom = chatRoomMySqlRepository.findById(save.getId()).get();

        assertThat(save.getChatRoomStatus())
                .isEqualTo(findChatRoom.getChatRoomStatus());
    }

}
