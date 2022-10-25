package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomEditInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomServiceResult;
import com.example.teamrocket.chatRoom.entity.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface ChatService {
    ChatRoomDto createRoom(Long userId, ChatRoomCreateInput param);

    List<ChatRoomDto> listRoom();

    ChatRoomDto editRoom(Long userId, String roomId, ChatRoomEditInput param);

    void deleteRoom(Long userId, String roomId);

    ChatRoomServiceResult enterRoom(String roomId, String password, Long userId);

    ChatRoomServiceResult leaveRoom(String roomId, Long userId);

    List<Message> getMessages(String roomId, LocalDateTime from, Long userId);

    void chatEnd(String roomId, Long userId);
}
