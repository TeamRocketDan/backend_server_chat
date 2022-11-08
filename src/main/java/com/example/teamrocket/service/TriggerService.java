package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TriggerService {
    private final ChatRoomMySqlRepository chatRoomMySqlRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    public void triggerChatRoom(){
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
        log.info("===== Delete ChatRoom Mysql Expired Date All Delete Done ====");
    }
}
