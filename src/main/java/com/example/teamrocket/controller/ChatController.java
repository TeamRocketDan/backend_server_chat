package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomEditInput;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.service.ChatService;
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
    public ResponseEntity<ChatRoomDto> createRoom(@RequestBody ChatRoomCreateInput param,
                                                            @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=1L;
//                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        ChatRoomDto chatRoom = chatService.createRoom(userId,param);

        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping("/room-list")
    public ResponseEntity<List<ChatRoomDto>> roomList(){
        List<ChatRoomDto> results = chatService.listRoom();
        return ResponseEntity.ok(results);
    }

    @PatchMapping("/room")
    public ResponseEntity<ChatRoomDto> editRoom(@RequestParam String roomId, @RequestBody ChatRoomEditInput param,
                                                @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        ChatRoomDto chatRoom = chatService.editRoom(userId,roomId,param);

        return ResponseEntity.ok(chatRoom);
    }

    @DeleteMapping("/room")
    public ResponseEntity<?> deleteRoom(@RequestParam String roomId
            , @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        chatService.deleteRoom(userId,roomId);

        return ResponseEntity.ok(null);
    }

    @PatchMapping("/room-enter")
    public ResponseEntity<List<Message>> enterRoom(@RequestParam String roomId, @RequestParam String password,
                                                   @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        chatService.enterRoom(roomId, password ,userId);
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/room-leave")
    public ResponseEntity<?> leaveRoom(@RequestParam String roomId, @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        chatService.leaveRoom(roomId, userId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/message")
    public ResponseEntity<List<Message>> getMessages(@RequestParam String roomId, @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        List<Message> messages = chatService.getMessages(roomId, userId);
        return ResponseEntity.ok(messages);
    }
}
