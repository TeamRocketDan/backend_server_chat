package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
@Slf4j
public class RabbitChatController {
    private final String CHAT_QUEUE_NAME = "chat.queue";
    private final RabbitTemplate template;
    private final String CHAT_EXCHANGE_NAME = "chat.exchange";
    private final RedisTemplateRepository redisTemplateRepository;
    private final Long systemUserId = 666L;

    @MessageMapping("chat.enter.{roomId}.{nickname}")
    public void enterMessage(@DestinationVariable String roomId,@DestinationVariable String nickname){
        Message message = Message.builder().userId(systemUserId).build();
        message.updateMessage(nickname + "님이 채팅방에 참여하였습니다.");

        message.updateRoomIdAndCreatedAt(roomId);
        template.convertAndSend(CHAT_EXCHANGE_NAME,"room."+roomId,message);
    }

    @MessageMapping("chat.message.{roomId}")
    public void send(Message message, @DestinationVariable String roomId, @Header("Authorization") String auth){
        System.out.println(auth);
        message.updateRoomIdAndCreatedAt(roomId);
        template.convertAndSend(CHAT_EXCHANGE_NAME,"room."+roomId,message);
    }

    @MessageMapping("chat.leave.{roomId}.{nickname}")
    public void leaveMessage(@DestinationVariable String roomId,@DestinationVariable String nickname){
        Message message = Message.builder().userId(systemUserId).build();
        message.updateRoomIdAndCreatedAt(roomId);
        message.updateMessage(nickname + "님이 채팅방에서 나가셨습니다.");
        template.convertAndSend(CHAT_EXCHANGE_NAME,"room."+roomId,message);
    }

    @RabbitListener(queues = CHAT_QUEUE_NAME)
    public void receiver(Message message){
        redisTemplateRepository.saveToLeft(message.getRoomId(), message);
    }
}
