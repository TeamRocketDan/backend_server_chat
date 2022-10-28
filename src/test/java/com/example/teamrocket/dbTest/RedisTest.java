package com.example.teamrocket.dbTest;

import com.example.teamrocket.chatRoom.entity.Message;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
        LocalDateTime to = now.plusSeconds(5);
        Duration between = Duration.between(now, to);
        redisTemplate.expire("abc",between.getSeconds(), TimeUnit.SECONDS);

        Thread.sleep(7000);

        Long abc = redisTemplate.opsForList().size("abc");

        Assertions.assertThat(abc).isEqualTo(0);
    }

    @Test
    @DisplayName("redis glob style get id test")
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

    @Test
    @DisplayName("redis get data 3days ago ")
    public void redisGet3DaysAgoData() throws Exception{
        // 테스트 변경 => 키 모두 들고와서 비교 해서 반납
        //init
        String roomId =  "roomIdTesting";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime twoDaysAgo = now.minusDays(2);
        LocalDateTime threeDaysAgo = now.minusDays(3);
        LocalDateTime fourDaysAgo = now.minusDays(4);
        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);

        List<LocalDateTime> timeStamps = new ArrayList<>(List.of(
                now,yesterday,twoDaysAgo,threeDaysAgo,fourDaysAgo,
                tenDaysAgo
        )
        );
        redisTemplate.opsForList().leftPush("backup", Message.builder().build());
        redisTemplate.opsForList().leftPush("backup1", Message.builder().build());
        redisTemplate.opsForList().leftPush("backup2", Message.builder().build());
        redisTemplate.opsForList().leftPush("backup3", Message.builder().build());
        //given
        for (LocalDateTime timeStamp : timeStamps) {
            String parser= timeStamp.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String redisKey = roomId+"#"+parser;
            String substring = parser.substring(6, 7);
            List<Message> messages = new ArrayList<>();
            for(int i=0; i<Integer.parseInt(substring)*100;i++){
                messages.add(Message.builder()
                        .senderName("SomePerson")
                        .message("Test Message "+ i)
                        .build());
            }
            //when
            redisTemplate.opsForList().leftPushAll(redisKey,messages);
        }

        System.out.println("======>>>>>>>  REDIS ALL DATA");
        Set<String> keys2 = redisTemplate.keys("*");
        for (String s : keys2) {
            System.out.println(s);
        }
        System.out.println("===================================");
        // Scan Build Needs specific number depends on today

        // O(1) 로 스캔해서 가져오기
        ScanOptions build = ScanOptions.scanOptions().match("*#*").build();
        Cursor<String> scan = redisTemplate.scan(build);
        List<String> result = new ArrayList<>();

        while (scan.hasNext()){
            String todayVerify= now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String yesterdayVerify = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String currentString = scan.next();
            String[] inputs = currentString.split("#");
            if(inputs.length < 2|| inputs[1].equals(todayVerify) || inputs[1].equals(yesterdayVerify)){continue;}
            System.out.println("===============================>>>>"+currentString);
            result.add(currentString);
        }

        // 21 일 배치 실패 22일 배치 실패 23일 배치 실패
        Assertions.assertThat(result.size()).isEqualTo(4);
    }
}