package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.*;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.utils.PagingResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ChatService {
    ChatRoomDto createRoom(Long userId, ChatRoomCreateInput param);

    PagingResponse<ChatRoomDto> listRoom(String rcate1, String rcate2, PageRequest pageRequest);

    ChatRoomDto editRoom(Long userId, String roomId, ChatRoomEditInput param);

    void deleteRoom(Long userId, String roomId);

    ChatRoomServiceResult enterRoom(String roomId, String password, Long userId);

    ChatRoomServiceResult leaveRoom(String roomId, Long userId);

    List<Message> getMessages(String roomId, Long userId);

    ChatRoomParticipantDto chatEnd(String roomId, Long userId);
}
