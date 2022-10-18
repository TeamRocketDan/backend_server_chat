package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class RabbitChatController {

    private final RabbitTemplate template;

    private final static String CHAT_EXCHANGE_NAME = "chat.exchange";
    private final static String CHAT_QUEUE_NAME = "chat.queue";

    @MessageMapping("chat.enter.{chatRoomId}")
    public void enterMessage(Message chat, @DestinationVariable String chatRoomId){

        chat.setMessage("입장하셨습니다.");
        chat.setCreatedAt(LocalDateTime.now());
        // message 저장

        template.convertAndSend(CHAT_EXCHANGE_NAME, "room." + chatRoomId, chat);
    }

    @MessageMapping("chat.message.{chatRoomId}")
    public void send(Message chat, @DestinationVariable String chatRoomId){

        template.convertAndSend(CHAT_EXCHANGE_NAME, "room." + chatRoomId, chat);

    }
}
