package com.example.teamrocket.chatRoom.repository.redis;

import com.example.teamrocket.chatRoom.domain.ChatRoomEditInput;
import com.example.teamrocket.chatRoom.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisTemplateRepository {
    @Autowired
    private RedisTemplate<String, Message> redisTemplate;
    public void setExpireDateOnRoom(String roomId,LocalDateTime startDate,LocalDateTime endDate){
        Message welcome = Message.builder()
                .senderName("TeamRocket")
                .message("Welcome !")
                .createdAt(LocalDateTime.now())
                .build();
        Duration between = Duration.between(startDate, endDate);

        redisTemplate.opsForList().leftPush(roomId,welcome);
        redisTemplate.expire(roomId,between.getSeconds(), TimeUnit.SECONDS);
    }
    public void updateExpireTime(String roomId, ChatRoomEditInput param) {
        Duration between = Duration.between(param.getStart_date(), param.getEnd_date());
        redisTemplate.expire(roomId,between.getSeconds(),TimeUnit.SECONDS);
    }

    public void deleteChatRoom(String roomId){
        redisTemplate.delete(roomId);
    }

    public void saveToLeft(String roomId, Message message){
        redisTemplate.opsForList().leftPush(roomId, message);
    }
    public List<Message> getAllMessageByRoomId(String roomId){
        return redisTemplate.opsForList().range(roomId, 0, -1);
    }

    public void deleteMessageByRoomId(String roomId){
        redisTemplate.delete(roomId);
    }

    public void flushAll(){
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        connection.flushAll();
    }
}
