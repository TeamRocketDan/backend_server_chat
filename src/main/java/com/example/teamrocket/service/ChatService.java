package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomInput;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ChatService {
    ChatRoomDto createRoom(Long userId, ChatRoomInput param);

    List<ChatRoomDto> listRoom();

    ChatRoomDto editRoom(Long userId, Long roomId, ChatRoomInput param);
}
