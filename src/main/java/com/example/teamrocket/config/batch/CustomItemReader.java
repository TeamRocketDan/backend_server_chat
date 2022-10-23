package com.example.teamrocket.config.batch;

import com.example.teamrocket.chatRoom.entity.Message;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
public class CustomItemReader  implements ItemReader<List<Message>>, StepExecutionListener {

    @Value("#{jobParameters[roomIdWithDayOfMonthYear]}")
    private String roomIdWithDayOfMonthYear;

    private StepExecution stepExecution;
    private final int READ_SIZE = 1000;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RedisTemplate<String,Message> redisTemplate;


    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }

    @Override
    public List<Message> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        Object count = jobContext.get("count");
        int count1 = 0;
        if(count != null){
            count1 = (int) count;
        }
        jobContext.put("count",count1+1);

        List<Message> list = redisTemplate.opsForList().range(roomIdWithDayOfMonthYear, count1 * READ_SIZE, (count1 + 1) * READ_SIZE);
        if(list.size() == 0) return null;
        return list;
    }
}
