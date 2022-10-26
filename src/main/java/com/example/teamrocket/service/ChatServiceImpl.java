package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.*;
import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.mongo.ChatRoomMongoRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomParticipantRepository;
import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
import com.example.teamrocket.error.exception.ChatRoomException;
import com.example.teamrocket.error.exception.UserException;
import com.example.teamrocket.user.entity.User;
import com.example.teamrocket.user.repository.UserRepository;
import com.example.teamrocket.utils.PagingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final RedisTemplateRepository redisTemplateRepository;

    @Override
    public ChatRoomDto createRoom(Long userId, ChatRoomCreateInput param) {
        User user = userRepository.findById(userId).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));

        if(param.getEnd_date().isBefore(param.getStart_date())){
            throw new ChatRoomException(TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE);
        }
        ChatRoom chatRoomMongo = ChatRoom.builder().build();
        ChatRoom chatRoomMongoSave = chatRoomMongoRepository.save(chatRoomMongo);

        ChatRoomMySql chatRoom = ChatRoomMySql.of(user, param);
        chatRoom.setId(chatRoomMongoSave.getChatRoomId());

        return ChatRoomDto.of(chatRoomMySqlRepository.save(chatRoom));
    }

    @Transactional(readOnly = true)
    @Override
    public PagingResponse<ChatRoomDto> listRoom(String rcate1, String rcate2, PageRequest pageRequest) {
        Page<ChatRoomMySql> chatRooms;
        if(rcate2 == null){
            chatRooms = chatRoomMySqlRepository.findAllByRcate1AndPrivateRoomFalseAndDeletedAtIsNullOrderByStart_date(rcate1,pageRequest);
        }else{
            chatRooms = chatRoomMySqlRepository.findAllByRcate1AndRcate2AndPrivateRoomFalseAndDeletedAtIsNullOrderByStart_date(rcate1,rcate2,pageRequest);
        }

        return PagingResponse.fromEntity(
                chatRooms.map(chatRoom -> ChatRoomDto.of(chatRoom))
        );
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
        redisTemplateRepository.updateExpireTime(roomId,param);

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
    public List<Message> getMessages(String roomId, Long userId) {
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        ChatRoomParticipant participant = chatRoomParticipantRepository
                .findByChatRoomMySqlAndUserId(chatRoom, userId).orElseThrow(
                        () -> new ChatRoomException(NOT_PARTICIPATED_USER));
        //redis 에서 페이징 처리해서 가져와야함 parmeter 로 pageable 필요
        //redis 에서 가져오는걸로 변경 페이징 필요 (요일별로 가져온다던지)


        ChatRoom chatRoomMongo = chatRoomMongoRepository.findById(roomId).orElseThrow(
                ()->new ChatRoomException(CHAT_ROOM_NOT_FOUND));

//        List<Message> messages = chatRoomMongo.getMessages();
        List<Message> messages = null;

        messages = messages.stream().takeWhile(
                x->x.getCreatedAt().isAfter(participant.getLeftAt())).collect(Collectors.toList());
        Collections.reverse(messages);

        return messages;
    }

    @Override
    public ChatRoomParticipantDto chatEnd(String roomId, Long userId) {
        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findById(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        var participant = chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom, userId)
                .orElseThrow(() -> new ChatRoomException(NOT_PARTICIPATED_USER));

        participant.setLeftAt(LocalDateTime.now());
        return ChatRoomParticipantDto.of(participant);
    }
}
