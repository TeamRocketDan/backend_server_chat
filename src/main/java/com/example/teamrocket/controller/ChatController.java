package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomInput;
import com.example.teamrocket.service.ChatService;
import com.example.teamrocket.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/chat")
@Log4j2
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/room")
    public ResponseEntity<ChatRoomDto> createRoom(@RequestBody ChatRoomInput param, @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
//                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        ChatRoomDto chatRoom = chatService.createRoom(userId,param);

        return ResponseEntity.ok(chatRoom);
    }



}
