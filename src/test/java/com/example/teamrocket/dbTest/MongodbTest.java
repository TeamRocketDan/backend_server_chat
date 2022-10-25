package com.example.teamrocket.dbTest;

import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.DayOfMessages;
import com.example.teamrocket.chatRoom.entity.Message;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@SpringBootTest
public class MongodbTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("MongoDb insert test")
    void insertTest() throws Exception{

        ChatRoom cr = ChatRoom.builder().build();

        ChatRoom chatRoom = mongoTemplate.save(cr);

        String dayOfMessageStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dayOfMessageStr2 = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        DayOfMessages dayOfMessages = DayOfMessages.builder()
                .id(dayOfMessageStr+"#"+cr.getChatRoomId())
                .build();

        DayOfMessages dayOfMessages2 = DayOfMessages.builder()
                .id(dayOfMessageStr2+"#"+cr.getChatRoomId())
                .build();

        DayOfMessages dayOfMessages1 = mongoTemplate.save(dayOfMessages);
        DayOfMessages dayOfMessages22 = mongoTemplate.save(dayOfMessages2);

        Update update = new Update();
        update.push("dayOfMessages").each(List.of(dayOfMessages1,dayOfMessages22));
        Criteria criteria = where("_id").is(chatRoom.getChatRoomId());
        mongoTemplate.updateFirst(Query.query(criteria),update,"chatRoom");



        for (int i = 0; i < 4; i++) {
            Message m = Message.builder()
                    .message("HolyMoly "+i)
                    .senderName("abc "+i)
                    .createdAt(LocalDateTime.now())
                    .build();
            mongoTemplate.save(m);

            Update update1 = new Update();
            update1.push("messages",m.getId());
            Criteria criteria1 = where("_id").is(dayOfMessages1.getId());
            mongoTemplate.updateFirst(Query.query(criteria1),update1,"dayOfMessages");
        }

        MatchOperation match = new MatchOperation(
                Criteria.where("_id").is(chatRoom.getChatRoomId())
        );
        Aggregation aggregate = Aggregation.newAggregation(match,
                Aggregation.project()
                        .and("dayOfMessages").project("size").as("countDayOfMessages")
        );
        List<Document> mappedResults = mongoTemplate.aggregate(aggregate, ChatRoom.class, Document.class).getMappedResults();
        assertThat(mappedResults.get(0).get( "countDayOfMessages")).isEqualTo(2);
    }
}

