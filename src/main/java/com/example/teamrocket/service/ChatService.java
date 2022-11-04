package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.*;
import com.example.teamrocket.utils.MessagePagingResponse;
import com.example.teamrocket.utils.PagingResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface ChatService {
    ChatRoomDto createRoom(ChatRoomCreateInput param);

    PagingResponse<ChatRoomDto> listRoom(String rcate1, String rcate2, Pageable pageRequest);

    ChatRoomDto editRoom(String roomId, ChatRoomEditInput param);

    void deleteRoom(String roomId);

    ChatRoomServiceResult enterRoom(String roomId);

    ChatRoomServiceResult leaveRoom(String roomId);

    MessagePagingResponse<MessageDto> getMessages(String roomId, LocalDate date, Integer page, Integer size);

    MessagePagingResponse<MessageDto> getMessagesMongo(String roomId,Integer page, Integer size);

    ChatRoomParticipantDto chatEnd(String roomId);

    PagingResponse<ChatRoomDto> myListRoom(Pageable pageRequest);
}
