package com.example.teamrocket.config.scheduler;

import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomParticipantRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleConfig {
    private ChatRoomMySqlRepository chatRoomMySqlRepository;
    private ChatRoomParticipantRepository chatRoomParticipantRepository;

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
            log.info(" ===============>>>>>>>>>>>>>>> Batch running ... current : "+ cnt++ +" / "+size);
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

    @Scheduled(cron = "0 0 0 ? * *",zone = "Asia/Seoul")
    public void deleteExpiredChatRoomOnMysql(){
        // 1000 ?????? ???????????? ????????? ????????? ?????? Delete ?????????
        int cnt =0;
        log.info("Delete Start .....");
        while(true){
            //?????? ?????? ?????? ????????????
            log.info("Deleting ..... count is : "+cnt++);
            List<ChatRoomMySql> expiredDate =
                    chatRoomMySqlRepository.findExpiredDate(LocalDate.now());

            if(expiredDate.size() == 0 || expiredDate.isEmpty()){
                break;
            }
            //?????? ??? ?????? ?????? ????????? ????????????
            List<ChatRoomParticipant> allByChatRoomMySqlExpired =
                    chatRoomParticipantRepository
                            .findAllByChatRoomMySqlExpired(expiredDate);
            //?????? ????????? ?????? ??????
            chatRoomParticipantRepository
                    .deleteAllByChatRoomParticipants(allByChatRoomMySqlExpired);
            //?????? ??? ??????
            chatRoomMySqlRepository
                    .deleteAllByChatRoomList(expiredDate);
        }
        log.info("Delete ChatRoom Mysql Expired Date All Delete Done");
    }

}
