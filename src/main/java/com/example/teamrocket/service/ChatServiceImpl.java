package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomInput;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.repository.ChatRoomMySqlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    @Transactional(readOnly = true)
    @Override
    public List<ChatRoomDto> listRoom() {

        return ChatRoomDto.of(chatRoomMySqlRepository.findAll());
    }

    @Transactional
    @Override
    public ChatRoomDto editRoom(Long userId, Long roomId, ChatRoomInput param) {

        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new RuntimeException(""));

        if (!chatRoom.getUserId().equals(userId)) {
            throw new RuntimeException("");
        }

        chatRoom.setTitle(param.getTitle());
        chatRoom.setStart_date(param.getStart_date());
        chatRoom.setEnd_date(param.getEnd_date());
        chatRoom.setMaxParticipant(param.getMaxParticipant());
        chatRoom.setPrivateRoom(param.isPrivateRoom());
        chatRoom.setPassword(param.getPassword());
        chatRoom.setRcate1(param.getRcate1());
        chatRoom.setRcate2(param.getRcate2());
        chatRoom.setRcate3(param.getRcate3());
        chatRoom.setLongitude(param.getLongitude());
        chatRoom.setLatitude(param.getLatitude());
        chatRoom.setUpdatedAt(LocalDateTime.now());

        return ChatRoomDto.of(chatRoomMySqlRepository.save(chatRoom));
    }

    @Override
    public void deleteRoom(Long userId, Long roomId) {

        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new RuntimeException(""));

        if (!chatRoom.getUserId().equals(userId)) {
            throw new RuntimeException("");
        }

        chatRoom.setDeletedAt(LocalDateTime.now());
        chatRoomMySqlRepository.save(chatRoom);
    }
}
