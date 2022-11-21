package com.example.teamrocket.config.batch.message;

import com.example.teamrocket.chatRoom.entity.DayOfMessages;
import com.example.teamrocket.chatRoom.entity.Message;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@StepScope
public class CustomItemWriter implements ItemWriter<List<Message>> {

    @Value("#{jobParameters[roomIdWithDayOfMonthYear]}")
    private String roomIdWithDayOfMonthYear;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void write(List<? extends List<Message>> items) throws Exception {
        String[] parse = roomIdWithDayOfMonthYear.split("#");
        String roomId = parse[0];
        String dateOfChat = parse[1];

        pushDateOnChatRoom(roomId, dateOfChat);
        List<Message> willAddTotal = saveAllMessages(items);
        pushDateOnDayOfMessages(roomId+"#"+dateOfChat, willAddTotal);
    }

    private void pushDateOnDayOfMessages(String dateOfChat, List<Message> willAddTotal) {
        List<String> collect = willAddTotal.stream().map(x -> x.getId()).collect(Collectors.toList());
        Update update2 = new Update();
        update2.push("messages").each(collect);
        Criteria criteria2 = Criteria.where("_id").is(dateOfChat);
        mongoTemplate.updateFirst(Query.query(criteria2),update2,"dayOfMessages");
    }

    private List<Message> saveAllMessages(List<? extends List<Message>> items) {
        List<Message> willAddTotal = new ArrayList();
        for (List<Message> item : items) {
            willAddTotal.addAll(item);
        }
        mongoTemplate.insertAll(willAddTotal);
        return willAddTotal;
    }

    private void pushDateOnChatRoom(String roomId, String dateOfChat) {
        DayOfMessages dayOfMessages = DayOfMessages.builder()
                .id(roomId+"#"+dateOfChat)
                .build();

        mongoTemplate.save(dayOfMessages);

        Update update = new Update();
        update.push("dayOfMessages",dayOfMessages.getId());
        Criteria criteria = Criteria.where("_id").is(roomId);
        mongoTemplate.updateFirst(Query.query(criteria),update,"chatRoom");
    }
}
