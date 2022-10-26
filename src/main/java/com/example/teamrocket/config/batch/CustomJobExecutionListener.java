package com.example.teamrocket.config.batch;

import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@JobScope
public class CustomJobExecutionListener implements JobExecutionListener {

    @Value("#{jobParameters[roomIdWithDayOfMonthYear]}")
    private String roomIdWithDayOfMonthYear;

    @Autowired
    private RedisTemplateRepository redisTemplateRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        redisTemplateRepository.deleteMessageByRoomId(roomIdWithDayOfMonthYear);
    }
}
