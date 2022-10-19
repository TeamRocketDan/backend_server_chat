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

    ChatRoomDto editRoom(Long userId, String roomId, ChatRoomInput param);

    void deleteRoom(Long userId, String roomId);

    void enterRoom(String roomId, String password,Long userId);

    void leaveRoom(String roomId, Long userId);

    List<Message> getMessages(String roomId, Long userId);
}
