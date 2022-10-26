package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.domain.*;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.service.ChatService;
import com.example.teamrocket.utils.ApiUtils.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        Long userId=1L;
//                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        ChatRoomDto chatRoom = chatService.createRoom(userId,param);

        return ResponseEntity.ok(success(chatRoom));
    }

    @GetMapping("/room-list")
    public ResponseEntity<Page<ChatRoomMySql>> roomList(@RequestParam Integer page, @RequestParam Integer size,
                                                        @RequestParam String rcate1, @RequestParam String rcate2){
        PageRequest pageRequest = PageRequest.of(page,size);
        Page<ChatRoomMySql> results = chatService.listRoom(rcate1,rcate2,pageRequest);
        return ResponseEntity.ok(results);
    }

    @PatchMapping("/room/{roomId}")
    public ResponseEntity<ApiResult> editRoom(@PathVariable String roomId, @RequestBody ChatRoomEditInput param,
                                                @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        ChatRoomDto chatRoom = chatService.editRoom(userId,roomId,param);

        return ResponseEntity.ok(success(chatRoom));
    }

    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<ApiResult> deleteRoom(@PathVariable String roomId
            , @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        chatService.deleteRoom(userId,roomId);

        return ResponseEntity.ok(success(null));
    }

    @PatchMapping("/room-enter/{roomId}")
    public ResponseEntity<ApiResult> enterRoom(@PathVariable String roomId, @RequestParam String password,
                                                   @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        ChatRoomServiceResult result = chatService.enterRoom(roomId, password ,userId);
        return ResponseEntity.ok(success(result));
    }

    @PatchMapping("/room-leave/{roomId}")
    public ResponseEntity<ApiResult> leaveRoom(@PathVariable String roomId, @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        ChatRoomServiceResult result = chatService.leaveRoom(roomId, userId);
        return ResponseEntity.ok(success(result));
    }

    @GetMapping("/message/{roomId}")
    public ResponseEntity<ApiResult> getMessages(@PathVariable String roomId, @RequestParam LocalDateTime from
            , @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token); -> token에서 유저 정보 얻는 method 필요
        List<Message> messages = chatService.getMessages(roomId, from ,userId);
        return ResponseEntity.ok(success(messages));
    }

    @PatchMapping("/chat-end/{roomId}")
    public ResponseEntity<ApiResult> chatEnd(@PathVariable String roomId, @RequestHeader(name = "X_AUTH_TOKEN") String token){
        Long userId=0L;
        //                userId= provider.from(token);
        ChatRoomParticipantDto participantDto = chatService.chatEnd(roomId, userId);
        return ResponseEntity.ok(success(participantDto));

    }
}
