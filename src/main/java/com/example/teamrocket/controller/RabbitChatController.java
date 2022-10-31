package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;


@Controller
@RequiredArgsConstructor
@Slf4j
public class RabbitChatController {
    private final String CHAT_QUEUE_NAME = "chat.queue";
    private final RabbitTemplate template;
    private final String CHAT_EXCHANGE_NAME = "chat.exchange";
    private final RedisTemplateRepository redisTemplateRepository;


    @MessageMapping("chat.enter.{roomId}")
    public void enterMessage(Message message,@DestinationVariable String roomId){
        message.updateMessage(message.getSenderName() + "님이 채팅방에 참여하였습니다.");
        message.updateRoomIdAndCreatedAt(roomId);
        template.convertAndSend(CHAT_EXCHANGE_NAME,"room."+roomId,message);
    }

    @MessageMapping("chat.message.{roomId}")
    public void send(Message message, @DestinationVariable String roomId){
        message.updateRoomIdAndCreatedAt(roomId);
        template.convertAndSend(CHAT_EXCHANGE_NAME,"room."+roomId,message);
    }

    @MessageMapping("chat.leave.{roomId}")
    public void leaveMessage(Message message,@DestinationVariable String roomId){
        message.updateRoomIdAndCreatedAt(roomId);
        message.updateMessage(message.getMessage() + "님이 채팅방에서 나가셨습니다.");

        template.convertAndSend(CHAT_EXCHANGE_NAME,"room."+roomId,message);
    }

    @RabbitListener(queues = CHAT_QUEUE_NAME)
    public void receiver(Message message){
        redisTemplateRepository.saveToLeft(message.getRoomId(), message);
    }
}
