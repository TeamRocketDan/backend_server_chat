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
        // 1000 개씩 불러와서 지우게 만들어 주자 Delete 안하게
        int cnt =0;
        log.info("Delete Start .....");
        while(true){
            //지난 채팅 모두 가져오기
            log.info("Deleting ..... count is : "+cnt++);
            List<ChatRoomMySql> expiredDate =
                    chatRoomMySqlRepository.findExpiredDate(LocalDate.now());

            if(expiredDate.size() == 0 || expiredDate.isEmpty()){
                break;
            }
            //채팅 에 따른 모든 참여자 긁어오기
            List<ChatRoomParticipant> allByChatRoomMySqlExpired =
                    chatRoomParticipantRepository
                            .findAllByChatRoomMySqlExpired(expiredDate);
            //채팅 참여자 우선 삭제
            chatRoomParticipantRepository
                    .deleteAllByChatRoomParticipants(allByChatRoomMySqlExpired);
            //채팅 방 삭제
            chatRoomMySqlRepository
                    .deleteAllByChatRoomList(expiredDate);
        }
        log.info("Delete ChatRoom Mysql Expired Date All Delete Done");
    }

}
