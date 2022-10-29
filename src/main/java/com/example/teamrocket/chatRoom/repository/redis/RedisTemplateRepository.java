package com.example.teamrocket.chatRoom.repository.redis;

import com.example.teamrocket.chatRoom.domain.ChatRoomEditInput;
import com.example.teamrocket.chatRoom.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisTemplateRepository {
    @Autowired
    private RedisTemplate<String, Message> redisTemplate;
    public void setExpireDateOnRoom(String roomId, LocalDateTime endDate){
        Message welcome = Message.builder()
                .senderName("TeamRocket")
                .message("Welcome !")
                .createdAt(LocalDateTime.now())
                .build();
        Duration between = Duration.between(LocalDateTime.now(), endDate.plusDays(1));

        redisTemplate.opsForList().leftPush(roomId,welcome);
        redisTemplate.expire(roomId,between.getSeconds(), TimeUnit.SECONDS);
    }

    public Set<String> getAllTwoDaysAgoKeys(){
        LocalDateTime now = LocalDateTime.now();
        String todayVerify= now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String yesterdayVerify = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        ScanOptions build = ScanOptions.scanOptions().match("*").build();
        Cursor<String> scan = redisTemplate.scan(build);
        Set<String> result = new HashSet<>();

        while (scan.hasNext()){
            String currentString = scan.next();
            String[] inputs = currentString.split("#");
            if(inputs[1].equals(todayVerify) || inputs[1].equals(yesterdayVerify)){continue;}
            result.add(currentString);
        }

        return result;
    }
    public void updateExpireTime(String roomId, ChatRoomEditInput param) {
        Duration between = Duration.between(param.getStartDate().atStartOfDay(),
                param.getEndDate().plusDays(1).atStartOfDay());
        redisTemplate.expire(roomId,between.getSeconds(),TimeUnit.SECONDS);
    }

    public void deleteChatRoom(String roomId){
        //regex 로 바꾸기
       redisTemplate.delete(roomId);
    }

    public void saveToLeft(String roomId, Message message){
        String dayOfMessageStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisRoomId = roomId+"#"+dayOfMessageStr;
        redisTemplate.opsForList().leftPush(redisRoomId, message);
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

    public List<Message> getMessage(String roomId, int page, int size){
        return redisTemplate.opsForList().range(roomId, page *size, (page + 1) *size);
    }
}
