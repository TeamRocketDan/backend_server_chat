package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RabbitChatController {

    private final SimpMessagingTemplate template;
    private final RedisTemplateRepository redisTemplateRepository;


    @MessageMapping("/chat/enter/{roomId}")
    public void enterMessage(Message message,@DestinationVariable String roomId){

        message.setMessage(message.getSenderName() + "님이 채팅방에 참여하였습니다.");
        redisTemplateRepository.saveToLeft(roomId, message);

        template.convertAndSend( "sub/chat/room/"+roomId,message);
    }

    @MessageMapping("/chat/send/{roomId}")
    public void send(Message message, @DestinationVariable String roomId){

        template.convertAndSend("/sub/chat/room/"+roomId, message);

    }

    @MessageMapping("chat/leave/{roomId}")
    public void leaveMessage(Message message,@DestinationVariable String roomId){

        message.setMessage(message.getMessage() + "님이 채팅방에서 나가셨습니다.");
        redisTemplateRepository.saveToLeft(roomId, message);
    }
}
