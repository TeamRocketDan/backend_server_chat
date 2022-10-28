package com.example.teamrocket.dbTest;

import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.DayOfMessages;
import com.example.teamrocket.chatRoom.entity.Message;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Transactional
public class BatchTest {
        @Autowired
        private RedisTemplate<String, Message> redisTemplate;
        @Autowired
        private MongoTemplate mongoTemplate;
        @Autowired
        private WebTestClient webTestClient;

        private final int DATA_SIZE = 1000;
        private final String ROOM_ID = new ObjectId("63554516cbcad245718cb2ec").toString();

        @BeforeEach
        public void init(){
            webTestClient = webTestClient.mutate()
                    .responseTimeout(Duration.ofMillis(30000))
                    .build();
        }

        @Test
        @DisplayName("Create MongoDb ChatRoom")
        @Commit
        public void createChatRoom() throws Exception{
            ChatRoom chatRoom = ChatRoom.builder().chatRoomId(ROOM_ID).build();
            ChatRoom save = mongoTemplate.save(chatRoom);
            Assertions.assertThat(save.getChatRoomId()).isEqualTo(ROOM_ID);
        }

        @Test
        @DisplayName("Insert Data 2Days to Redis")
        public void saveTest() throws Exception{
            String dayOfMessageStr = LocalDateTime.now().minusDays(3).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String redisRoomId = ROOM_ID+"#"+dayOfMessageStr;

            String dayOfMessageStr2 = LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String redisRoomId2 = ROOM_ID+"#"+dayOfMessageStr2;
            for(int i=0;i<DATA_SIZE;i++){
                Message m1 = Message.builder()
                        .message("Message : "+i)
                        .roomId(ROOM_ID)
                        .createdAt(LocalDateTime.now())
                        .build();
                redisTemplate.opsForList().leftPush(redisRoomId, m1);
            }
            for(int i=0;i<DATA_SIZE*2;i++){
                Message m2 = Message.builder()
                        .message("Message : "+i)
                        .roomId(ROOM_ID)
                        .createdAt(LocalDateTime.now())
                        .build();
                redisTemplate.opsForList().leftPush(redisRoomId2, m2);
            }
            Long size = redisTemplate.opsForList().size(redisRoomId);
            Long size2 = redisTemplate.opsForList().size(redisRoomId2);
            Assertions.assertThat(size).isEqualTo((DATA_SIZE));
            Assertions.assertThat(size2).isEqualTo((DATA_SIZE*2));

        }


        @Test
        @DisplayName("BatchTest")
        @Commit
        public void batchTest() throws Exception {

            webTestClient
                    .get()
                    .uri("/batch")
                    .exchange()
                    .expectStatus().isOk();

            String dayOfMessageStr = LocalDateTime.now().minusDays(3).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            MatchOperation match = new MatchOperation(Criteria.where("_id").is(ROOM_ID+"#"+dayOfMessageStr));
            Aggregation aggregate = Aggregation.newAggregation(match, Aggregation.project().and("messages").project("size").as("count"));
            List<Document> mappedResults = mongoTemplate.aggregate(aggregate, DayOfMessages.class, Document.class).getMappedResults();
            int count = (int) mappedResults.get(0).get("count");
            Assertions.assertThat(count).isEqualTo(1000);

            String dayOfMessageStr2 = LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            MatchOperation match2 = new MatchOperation(Criteria.where("_id").is(ROOM_ID+"#"+dayOfMessageStr2));
            Aggregation aggregate2 = Aggregation.newAggregation(match2, Aggregation.project().and("messages").project("size").as("count"));
            List<Document> mappedResults2 = mongoTemplate.aggregate(aggregate2, DayOfMessages.class, Document.class).getMappedResults();
            int count2 = (int) mappedResults2.get(0).get("count");
            Assertions.assertThat(count2).isEqualTo(2000);
        }
}
