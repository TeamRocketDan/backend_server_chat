package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomParticipantRepository;
import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
import com.example.teamrocket.service.TriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/trigger")
@RequiredArgsConstructor
@Slf4j
public class ScheduleTriggerController {
    private final TriggerService triggerService;
    private final JobLauncher jobLauncher;
    private final Job messageJob;
    private final RedisTemplate<String, Message> redisTemplate;
    private final RedisTemplateRepository redisTemplateRepository;

    @GetMapping("batch")
    public void testBatchMessage() {
        int cnt = 1;
        Set<String> keys = redisTemplateRepository.getAllTwoDaysAgoKeys();
        log.info("This is keys Size : "+ keys.size());
        for (String key : keys) {
            Long size = redisTemplate.opsForList().size(key);
            log.info(" ===============>>>>>>>>>>>>>>> Batch running ... current : "+cnt++ +" / "+size);
            JobParameters jobParameter = new JobParametersBuilder()
                    .addString("roomIdWithDayOfMonthYear", key)
                    .addString("messageSize", String.valueOf(size))
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();
            try {
                jobLauncher.run(messageJob, jobParameter);
            } catch (JobExecutionAlreadyRunningException | JobRestartException |
                     JobInstanceAlreadyCompleteException |
                     JobParametersInvalidException e) {
                throw new RuntimeException(e);
            }
        }
        log.info(" ===============>>>>>>>>>>>>>>> Batch Done");
    }
    @GetMapping("chatroom")
    public void testChatRoom(){
        triggerService.triggerChatRoom();
    }
}
