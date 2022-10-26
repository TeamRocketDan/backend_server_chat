package com.example.teamrocket.config.scheduler;

import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleConfig {

    private final JobLauncher jobLauncher;
    private final Job messageJob;
    private final RedisTemplate<String, Message> redisTemplate;
    private final RedisTemplateRepository redisTemplateRepository;

    @Scheduled(cron = "0 0 5 ? * *",zone = "Asia/Seoul")
    public void pushMessageToMongoFromRedis(){
        int cnt = 1;
        Set<String> keys = redisTemplateRepository.getAllTwoDaysAgoKeys();

        for (String key : keys) {
            Long size = redisTemplate.opsForList().size(key);
            log.info(" ===============>>>>>>>>>>>>>>> Batch running ... current : "+cnt+" / "+size);
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
        cnt++;
    }
}
