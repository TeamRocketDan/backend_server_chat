package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomInput;
import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.mongo.ChatRoomMongoRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomParticipantRepository;
import com.example.teamrocket.user.entity.User;
import com.example.teamrocket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService{

    private final UserRepository userRepository;
    private final ChatRoomMySqlRepository chatRoomMySqlRepository;
    private final ChatRoomMongoRepository chatRoomMongoRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    @Transactional
    @Override
    public ChatRoomDto createRoom(Long userId, ChatRoomInput param) {
        User user = userRepository.findById(userId).orElseThrow(
                ()->new RuntimeException("유저를 찾을 수 없습니다."));

        ChatRoomMySql chatRoom = ChatRoomMySql.of(user, param);
        return ChatRoomDto.of(chatRoomMySqlRepository.save(chatRoom));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatRoomDto> listRoom() {
        var chatRooms = chatRoomMySqlRepository.findAll().stream().filter(x->(!x.isPrivateRoom()|| x.getDeletedAt()!=null)).collect(Collectors.toList());
        List<ChatRoomDto> results = new ArrayList<>(chatRooms.size());
        for(ChatRoomMySql chatRoom : chatRooms){
            ChatRoomDto chatRoomDto = ChatRoomDto.of(chatRoom);
            chatRoomDto.setCurParticipant(chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom).size());
            results.add(chatRoomDto);
        }

        return results;
    }

    @Transactional
    @Override
    public ChatRoomDto editRoom(Long userId, String roomId, ChatRoomInput param) {
        User user = userRepository.findById(userId).orElseThrow(
                ()->new RuntimeException("유저를 찾을 수 없습니다."));
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new RuntimeException("방을 찾을 수 없습니다."));
        List<ChatRoomParticipant> participants =
                chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom);

        if (!chatRoom.getOwner().equals(user)) {
            throw new RuntimeException("방장이 아닙니다.");
        }


        if(param.getStart_date().isBefore(LocalDateTime.now())){
            throw new RuntimeException("여행 시작날짜는 현재 날짜 이후여야합니다.");
        }

        if(param.getEnd_date().isBefore(param.getStart_date())){
            throw new RuntimeException("여행 시작 날짜는 여행 끝 날짜 이전이여야 합니다.");
        }

        if(param.getMaxParticipant()<participants.size()){
            throw new RuntimeException("현재 채팅방 인원보다 채팅방 인원을 적게 수정할 수 없습니다.");
        }

        chatRoom.update(param.getTitle(),param.getStart_date(),param.getEnd_date(),
                param.getMaxParticipant(), param.isPrivateRoom(), param.getPassword());


        return ChatRoomDto.of(chatRoom);
    }

    @Override
    public void deleteRoom(Long userId, String roomId) {
        User user = userRepository.findById(userId).orElseThrow(
                ()->new RuntimeException("유저를 찾을 수 없습니다."));
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new RuntimeException("방을 찾을 수 없습니다."));

        if (!chatRoom.getOwner().equals(user)) {
            throw new RuntimeException("방장이 아닙니다.");
        }

        chatRoom.delete();
        chatRoomMySqlRepository.save(chatRoom);
        chatRoomParticipantRepository.deleteAllByChatRoomMySql(chatRoom);
    }

    @Override
    public void enterRoom(String roomId,String password ,Long userId) {
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new RuntimeException("방을 찾을 수 없습니다."));

        List<ChatRoomParticipant> participants =
                chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom);

        if(participants.stream().anyMatch(x->x.getUserId().equals(userId))){
            throw new RuntimeException("이미 방에 참가한 사람입니다.");
        } else if(participants.size() < chatRoom.getMaxParticipant()){

            if(!chatRoom.getPassword().equals(password)){
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }

            ChatRoomParticipant participant = ChatRoomParticipant.builder()
                    .chatRoomMySql(chatRoom)
                    .userId(userId)
                    .build();
            chatRoomParticipantRepository.save(participant);
        } else{
            throw new RuntimeException("정원을 넘어 들어갈 수 없습니다.");
        }
    }

    @Override
    public void leaveRoom(String roomId, Long userId) {
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new RuntimeException("방을 찾을 수 없습니다."));

        ChatRoomParticipant participant =
                chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,userId)
                                .orElseThrow(()->new RuntimeException("방에 참가한 이력이 없습니다."));

        chatRoomParticipantRepository.delete(participant);

    }

    @Override
    public List<Message> getMessages(String roomId, Long userId) {
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new RuntimeException("방을 찾을 수 없습니다."));

        ChatRoomParticipant participant =
                chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom, userId)
                        .orElseThrow(() -> new RuntimeException("방에 참가한 이력이 없습니다."));


        ChatRoom chatRoomMongo = chatRoomMongoRepository.findById(roomId).orElseThrow(
                ()->new RuntimeException("방을 찾을 수 없습니다."));

        List<Message> messages = chatRoomMongo.getMessages();

        messages = messages.stream().takeWhile(
                x->x.getCreatedAt().isAfter(participant.getLeftAt())).collect(Collectors.toList());
        Collections.reverse(messages);

        return messages;
    }
}
