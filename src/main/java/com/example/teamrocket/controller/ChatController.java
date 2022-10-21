package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomEditInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomServiceResult;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.service.ChatService;
import com.example.teamrocket.utils.ApiUtils.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.teamrocket.utils.ApiUtils.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/chat")
@Log4j2
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/room")
    public ResponseEntity<ApiResult> createRoom(@RequestBody ChatRoomCreateInput param,
                                                         @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
//                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        ChatRoomDto chatRoom = chatService.createRoom(userId,param);

        return ResponseEntity.ok(success(chatRoom));
    }

    @GetMapping("/room-list")
    public ResponseEntity<List<ChatRoomDto>> roomList(){
        List<ChatRoomDto> results = chatService.listRoom();
        return ResponseEntity.ok(results);
    }

    @PatchMapping("/room")
    public ResponseEntity<ApiResult> editRoom(@RequestParam String roomId, @RequestBody ChatRoomEditInput param,
                                                @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        ChatRoomDto chatRoom = chatService.editRoom(userId,roomId,param);

        return ResponseEntity.ok(success(chatRoom));
    }

    @DeleteMapping("/room")
    public ResponseEntity<ApiResult> deleteRoom(@RequestParam String roomId
            , @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        chatService.deleteRoom(userId,roomId);

        return ResponseEntity.ok(success(null));
    }

    @PatchMapping("/room-enter")
    public ResponseEntity<ApiResult> enterRoom(@RequestParam String roomId, @RequestParam String password,
                                                   @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        ChatRoomServiceResult result = chatService.enterRoom(roomId, password ,userId);
        return ResponseEntity.ok(success(result));
    }

    @PatchMapping("/room-leave")
    public ResponseEntity<ApiResult> leaveRoom(@RequestParam String roomId, @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        ChatRoomServiceResult result = chatService.leaveRoom(roomId, userId);
        return ResponseEntity.ok(success(result));
    }

    @GetMapping("/message")
    public ResponseEntity<List<Message>> getMessages(@RequestParam String roomId, @RequestParam LocalDateTime from
            , @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        List<Message> messages = chatService.getMessages(roomId, from ,userId);
        return ResponseEntity.ok(messages);
    }
}
