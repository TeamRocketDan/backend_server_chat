package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.domain.*;
import com.example.teamrocket.service.ChatService;
import com.example.teamrocket.utils.ApiUtils.ApiResult;
import com.example.teamrocket.utils.MessagePagingResponse;
import com.example.teamrocket.utils.PagingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.example.teamrocket.utils.ApiUtils.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/chat")
@Log4j2
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/room")
    public ResponseEntity<ApiResult<ChatRoomDto>> createRoom(@Valid @RequestBody ChatRoomCreateInput param){
        ChatRoomDto chatRoom = chatService.createRoom(param);

        return ResponseEntity.ok(success(chatRoom));
    }

    @GetMapping("/room-list")
    public ResponseEntity<ApiResult<PagingResponse<ChatRoomDto>>> roomList(@RequestParam Integer page, @RequestParam Integer size,
                                                        @RequestParam(required = false) String rcate1, @RequestParam(required = false) String rcate2){
        PageRequest pageRequest = PageRequest.of(page,size);
        PagingResponse<ChatRoomDto> results = chatService.listRoom(rcate1,rcate2,pageRequest);
        return ResponseEntity.ok(success(results));
    }

    @GetMapping("/my-room-list")
    public ResponseEntity<ApiResult<PagingResponse<ChatRoomDto>>> myListRoom(@RequestParam Integer page, @RequestParam Integer size){

        PageRequest pageRequest = PageRequest.of(page,size);
        PagingResponse<ChatRoomDto> results = chatService.myListRoom(pageRequest);
        return ResponseEntity.ok(success(results));
    }

    @PatchMapping("/room/{roomId}")
    public ResponseEntity<ApiResult<ChatRoomDto>> editRoom(@PathVariable String roomId, @Valid @RequestBody ChatRoomEditInput param){
        ChatRoomDto chatRoom = chatService.editRoom(roomId,param);

        return ResponseEntity.ok(success(chatRoom));
    }

    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<ApiResult<?>> deleteRoom(@PathVariable String roomId){
        chatService.deleteRoom(roomId);

        return ResponseEntity.ok(success(null));
    }

    @PatchMapping("/room-enter/{roomId}")
    public ResponseEntity<ApiResult<ChatRoomEnterResult>> enterRoom(@PathVariable String roomId){
        ChatRoomEnterResult result = chatService.enterRoom(roomId);
        return ResponseEntity.ok(success(result));
    }

    @PatchMapping("/room-leave/{roomId}")
    public ResponseEntity<ApiResult<ChatRoomServiceResult>> leaveRoom(@PathVariable String roomId){
        ChatRoomServiceResult result = chatService.leaveRoom(roomId);
        return ResponseEntity.ok(success(result));
    }

    @GetMapping("/room/info/{roomId}")
    public ResponseEntity<ApiResult<RoomInfoDto>> getInfo(@PathVariable String roomId){
        RoomInfoDto result = chatService.getRoomInfo(roomId);
        return ResponseEntity.ok(success(result));
    }

    @GetMapping("/message/{roomId}")
    public ResponseEntity<ApiResult<MessagePagingResponse<MessageDto>>> getMessages(
            @PathVariable String roomId,
            @RequestParam Integer page, @RequestParam Integer size){

        MessagePagingResponse<MessageDto> messages = chatService.getMessages(roomId,page,size);
        log.info("Messages get total "+ messages.getContent().size());
        return ResponseEntity.ok(success(messages));
    }

    @PatchMapping("/chat-end/{roomId}")
        public ResponseEntity<ApiResult<ChatRoomServiceResult>> chatEnd(@PathVariable String roomId){
        ChatRoomServiceResult result = chatService.chatEnd(roomId);
        return ResponseEntity.ok(success(result));
    }
}

