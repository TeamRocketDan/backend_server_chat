package com.example.teamrocket.dbTest;

import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
import net.bytebuddy.asm.Advice;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate<String, Message> redisTemplate;

    @Test
    @DisplayName("redis expire date init")
    public void redisInitExpireDateTest() throws Exception{
        Message build = Message.builder()
                .senderName("TeamRocket")
                .message("Welcome Message")
                .createdAt(LocalDateTime.now())
                .build();
        redisTemplate.opsForList().leftPush("abc", build);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime to = now.plusSeconds(3);
        Duration between = Duration.between(now, to);
        redisTemplate.expire("abc",between.getSeconds(), TimeUnit.SECONDS);

        Thread.sleep(5000);

        Long abc = redisTemplate.opsForList().size("abc");

        Assertions.assertThat(abc).isEqualTo(0);
    }

    @Test
    @DisplayName("redis regex get id test")
    public void redisRegexGetIdTest() throws Exception{
        String s = UUID.randomUUID().toString();
        int sizeTest = 100;
        for (int i = 0; i < sizeTest; i++) {
            Message build = Message.builder()
                    .senderName("TeamRocket")
                    .message("Welcome Message" + i)
                    .createdAt(LocalDateTime.now())
                    .build();

            redisTemplate.opsForList().leftPush(s+i, build);
        }

        Set<String> keys = redisTemplate.keys(s+"*");
        Assertions.assertThat(keys.size()).isEqualTo(sizeTest);
    }
}
