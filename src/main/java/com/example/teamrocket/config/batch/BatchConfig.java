package com.example.teamrocket.config.batch;

import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.config.batch.message.CustomItemReader;
import com.example.teamrocket.config.batch.message.CustomItemWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final CustomItemReader customItemReader;
    private final CustomItemWriter customItemWriter;
    private final CustomJobExecutionListener customJobExecutionListener;

    @Bean
    @StepScope
    public ExecutionContextPromotionListener promotionListener(){
        ExecutionContextPromotionListener executionContextPromotionListener = new ExecutionContextPromotionListener();
        executionContextPromotionListener.setKeys(new String[]{"count"});
        return executionContextPromotionListener;
    }

    @Bean
    public Job messageJob(){
        return jobBuilderFactory.get("Batch Messages : "+ LocalDateTime.now().toString())
                .flow(messageStep(null))
                .end()
                .listener(customJobExecutionListener)
                .build();
    }

    @Bean
    @JobScope
    public Step messageStep(
            @Value("#{jobParameters[roomIdWithDayOfMonthYear]}") String dayOfMonthYear
    ){
        return stepBuilderFactory.get("Batch Messages step : "+dayOfMonthYear)
                .<List<Message>, List<Message>>chunk(1000)
                .reader(customItemReader)
                .writer(customItemWriter)
                .listener(promotionListener())
                .build();
    }
}
