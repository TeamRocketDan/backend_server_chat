package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.*;
import com.example.teamrocket.utils.MessagePagingResponse;
import com.example.teamrocket.utils.PagingResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface ChatService {
    ChatRoomDto createRoom(ChatRoomCreateInput param);

    PagingResponse<ChatRoomDto> listRoom(String rcate1, String rcate2, Pageable pageRequest);

    ChatRoomDto editRoom(String roomId, ChatRoomEditInput param);

    void deleteRoom(String roomId);

    ChatRoomEnterResult enterRoom(String roomId);

    ChatRoomServiceResult leaveRoom(String roomId);

    RoomInfoDto getRoomInfo(String roomId);

    MessagePagingResponse<MessageDto> getMessages(String roomId,Integer page, Integer size);

    PagingResponse<ChatRoomDto> myListRoom(Pageable pageRequest);

    ChatRoomServiceResult chatEnd(String roomId);
}
