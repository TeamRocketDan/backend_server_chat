package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomInput;
import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.repository.ChatRoomMySqlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService{

    private final ChatRoomMySqlRepository chatRoomMySqlRepository;

    @Transactional
    @Override
    public ChatRoomDto createRoom(Long userId, ChatRoomInput param) {

        ChatRoomMySql chatRoom = ChatRoomMySql.of(userId, param);
        return ChatRoomDto.of(chatRoomMySqlRepository.save(chatRoom));
    }
}
