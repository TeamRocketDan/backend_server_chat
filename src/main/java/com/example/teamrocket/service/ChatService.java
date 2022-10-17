package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomInput;
import com.example.teamrocket.chatRoom.entity.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ChatService {
    ChatRoomDto createRoom(Long userId, ChatRoomInput param);

    List<ChatRoomDto> listRoom();

    ChatRoomDto editRoom(Long userId, Long roomId, ChatRoomInput param);

    void deleteRoom(Long userId, Long roomId);

    List<Message> enterRoom(Long roomId, Long userId);

    void leaveRoom(Long roomId, Long userId);
}
