package com.example.teamrocket.chatRoom.repository;

import com.example.teamrocket.chatRoom.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisTemplateRepository {
    @Autowired
    private RedisTemplate<String, Message> redisTemplate;

    public void saveToLeft(String roomId,Message message){
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
