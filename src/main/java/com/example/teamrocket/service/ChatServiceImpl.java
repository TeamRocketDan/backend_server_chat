package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomInput;
import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.ChatRoomMongoRepository;
import com.example.teamrocket.chatRoom.repository.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.ChatRoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService{

    private final ChatRoomMySqlRepository chatRoomMySqlRepository;
    private final ChatRoomMongoRepository chatRoomMongoRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

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
                () -> new RuntimeException("방을 찾을 수 없습니다."));

        if (!chatRoom.getUserId().equals(userId)) {
            throw new RuntimeException("방장이 아닙니다.");
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
                () -> new RuntimeException("방을 찾을 수 없습니다."));

        if (!chatRoom.getUserId().equals(userId)) {
            throw new RuntimeException("방장이 아닙니다.");
        }

        chatRoom.setDeletedAt(LocalDateTime.now());
        chatRoomMySqlRepository.save(chatRoom);
    }

    @Override
    public List<Message> enterRoom(Long roomId, Long userId) {
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new RuntimeException("방을 찾을 수 없습니다."));

        Optional<ChatRoomParticipant> optionalParticipant= chatRoom.getParticipants().stream().
                filter(x->x.getUserId().equals(userId)).findFirst();

        if(optionalParticipant.isPresent()){
            ChatRoom chatRoomMongo = chatRoomMongoRepository.findById(String.valueOf(roomId)).orElseThrow(
                    ()->new RuntimeException("방을 찾을 수 없습니다."));

            List<Message> messages = chatRoomMongo.getMessages();

            Collections.reverse(messages);
            messages = chatRoomMongo.getMessages().stream().takeWhile(
                    x->x.getCreatedAt().isAfter(optionalParticipant.get().getLeftAt())).collect(Collectors.toList());
            Collections.reverse(messages);

            return messages;
        } else if(chatRoom.getParticipants().size() < chatRoom.getMaxParticipant()){
            ChatRoomParticipant participant = new ChatRoomParticipant();
            participant.setChatRoomMySql(chatRoom);
            participant.setUserId(userId);
            chatRoomParticipantRepository.save(participant);
            return new ArrayList<>();
        } else{
            throw new RuntimeException("정원을 넘어 들어갈 수 없습니다.");
        }
    }
}
