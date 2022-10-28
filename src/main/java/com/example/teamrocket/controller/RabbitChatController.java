package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class RabbitChatController {

    private final RabbitTemplate template;
    private final String CHAT_EXCHANGE_NAME = "chat.exchange";
    private final RedisTemplateRepository redisTemplateRepository;


    @MessageMapping("chat.enter.{roomId}")
    public void enterMessage(Message message,@DestinationVariable String roomId){

        message.setMessage(message.getSenderName() + "님이 채팅방에 참여하였습니다.");
        redisTemplateRepository.saveToLeft(roomId, message);

        template.convertAndSend(CHAT_EXCHANGE_NAME,"room."+roomId,message);
    }

    @MessageMapping("chat.message.{roomId}")
    public void send(Message message, @DestinationVariable String roomId){
//        message.setCreatedAt(LocalDateTime.now());
        redisTemplateRepository.saveToLeft(roomId, message);
        template.convertAndSend(CHAT_EXCHANGE_NAME,"room."+roomId,message);

    }

    @MessageMapping("chat.leave.{roomId}")
    public void leaveMessage(Message message,@DestinationVariable String roomId){

        message.setMessage(message.getMessage() + "님이 채팅방에서 나가셨습니다.");
        redisTemplateRepository.saveToLeft(roomId, message);
        template.convertAndSend(CHAT_EXCHANGE_NAME,"room."+roomId,message);
    }
}
