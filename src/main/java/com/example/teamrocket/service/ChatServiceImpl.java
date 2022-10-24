package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.chatRoom.domain.ChatRoomEditInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomServiceResult;
import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.mongo.ChatRoomMongoRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomParticipantRepository;
import com.example.teamrocket.error.exception.ChatRoomException;
import com.example.teamrocket.error.exception.UserException;
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

import static com.example.teamrocket.error.type.ChatRoomErrorCode.*;
import static com.example.teamrocket.error.type.UserErrorCode.USER_NOT_FOUND;

@Transactional
@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService{

    private final UserRepository userRepository;
    private final ChatRoomMySqlRepository chatRoomMySqlRepository;
    private final ChatRoomMongoRepository chatRoomMongoRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    @Override
    public ChatRoomDto createRoom(Long userId, ChatRoomCreateInput param) {
        User user = userRepository.findById(userId).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));

        if(param.getEnd_date().isBefore(param.getStart_date())){
            throw new ChatRoomException(TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE);
        }


        ChatRoomMySql chatRoom = ChatRoomMySql.of(user, param);
        return ChatRoomDto.of(chatRoomMySqlRepository.save(chatRoom));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatRoomDto> listRoom() {
        var chatRooms = chatRoomMySqlRepository.findAll().stream()
                .filter(x->(!x.isPrivateRoom() &&  x.getDeletedAt()==null)).collect(Collectors.toList());
        List<ChatRoomDto> results = new ArrayList<>(chatRooms.size());
        for(ChatRoomMySql chatRoom : chatRooms){
            ChatRoomDto chatRoomDto = ChatRoomDto.of(chatRoom);
            chatRoomDto.setCurParticipant(chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom).size());
            results.add(chatRoomDto);
        }

        return results;
    }

    @Override
    public ChatRoomDto editRoom(Long userId, String roomId, ChatRoomEditInput param) {
        User user = userRepository.findById(userId).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));
        List<ChatRoomParticipant> participants =
                chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom);

        if (!chatRoom.getOwner().equals(user)) {
            throw new ChatRoomException(NOT_CHAT_ROOM_OWNER);
        }


        if(param.getStart_date().isBefore(LocalDateTime.now())){
            throw new ChatRoomException(START_DATE_MUST_BE_AFTER_TODAY);
        }

        if(param.getEnd_date().isBefore(param.getStart_date())){
            throw new ChatRoomException(TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE);
        }

        if(param.getMaxParticipant()<participants.size()){
            throw new ChatRoomException(MAX_PARTICIPANT_IS_TOO_SMALL);
        }

        chatRoom.update(param);


        return ChatRoomDto.of(chatRoom);
    }

    @Override
    public void deleteRoom(Long userId, String roomId) {
        User user = userRepository.findById(userId).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.getOwner().equals(user)) {
            throw new ChatRoomException(NOT_CHAT_ROOM_OWNER);
        }

        chatRoom.delete();
        chatRoomMySqlRepository.save(chatRoom);
        chatRoomParticipantRepository.deleteAllByChatRoomMySql(chatRoom);
    }


    @Override
    public ChatRoomServiceResult enterRoom(String roomId, String password , Long userId) {
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));
        List<ChatRoomParticipant> participants =
                chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom);

        if(participants.stream().anyMatch(x->x.getUserId().equals(userId))){
            throw new ChatRoomException(ALREADY_PARTICIPATE);
        } else if(participants.size() < chatRoom.getMaxParticipant()){

            if(!chatRoom.getPassword().equals(password)){
                throw new ChatRoomException(PASSWORD_NOT_MATCH);
            }

            ChatRoomParticipant participant = ChatRoomParticipant.builder()
                    .chatRoomMySql(chatRoom)
                    .userId(userId)
                    .build();
            chatRoomParticipantRepository.save(participant);
            return new ChatRoomServiceResult(roomId,userId);
        } else{
            throw new ChatRoomException(EXCEED_MAX_PARTICIPANTS);
        }
    }

    @Override
    public ChatRoomServiceResult leaveRoom(String roomId, Long userId) {
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        ChatRoomParticipant participant =
                chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,userId)
                                .orElseThrow(()->new ChatRoomException(NOT_PARTICIPATED_USER));

        chatRoomParticipantRepository.delete(participant);
        return new ChatRoomServiceResult(roomId,userId);
    }

    @Override
    public List<Message> getMessages(String roomId,LocalDateTime from, Long userId) {
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom, userId)
                        .orElseThrow(() -> new ChatRoomException(NOT_PARTICIPATED_USER));


        ChatRoom chatRoomMongo = chatRoomMongoRepository.findById(roomId).orElseThrow(
                ()->new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        List<Message> messages = chatRoomMongo.getMessages();

        messages = messages.stream().takeWhile(
                x->x.getCreatedAt().isAfter(from)).collect(Collectors.toList());
        Collections.reverse(messages);

        return messages;
    }
}
