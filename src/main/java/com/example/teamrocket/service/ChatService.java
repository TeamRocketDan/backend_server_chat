package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomInput;
import com.example.teamrocket.user.User;
import org.springframework.stereotype.Service;

@Service
public interface ChatService {
    ChatRoomDto createRoom(Long userId, ChatRoomInput param);
}
