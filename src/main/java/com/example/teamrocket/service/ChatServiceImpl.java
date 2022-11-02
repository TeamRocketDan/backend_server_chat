package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.*;
import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.DayOfMessages;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.mongo.ChatRoomMongoRepository;
import com.example.teamrocket.chatRoom.repository.mongo.MessageRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomParticipantRepository;
import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
import com.example.teamrocket.error.exception.ChatRoomException;
import com.example.teamrocket.error.exception.UserException;
import com.example.teamrocket.user.entity.User;
import com.example.teamrocket.user.repository.UserRepository;
import com.example.teamrocket.utils.CommonRequestContext;
import com.example.teamrocket.utils.MessagePagingResponse;
import com.example.teamrocket.utils.PagingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final MessageRepository messageRepository;
    private final CommonRequestContext commonRequestContext;

    @Override
    public ChatRoomDto createRoom(ChatRoomCreateInput param) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));

        if(param.getEndDate().isBefore(param.getStartDate())){
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
        Page<ChatRoomMySql> chatRooms
                = chatRoomMySqlRepository.findAllByRcate1AndRcate2AndPrivateRoomFalseAndDeletedAtIsNullOrderByStartDate(rcate1,rcate2,pageRequest);

        List<ChatRoomDto> contents = new ArrayList<>(chatRooms.getContent().size());
        for(ChatRoomMySql chatRoom:chatRooms.getContent()){
            ChatRoomDto chatRoomDto = ChatRoomDto.of(chatRoom);
            chatRoomDto.setCurParticipant(chatRoom.getParticipants().size());

            User owner = chatRoom.getOwner();
            chatRoomDto.setOwnerInfo(owner.getNickname(),owner.getProfileImage());
            contents.add(chatRoomDto);
        }

        PagingResponse<ChatRoomDto> result = PagingResponse.fromEntity(chatRooms);
        result.setContent(contents);
        return result;
    }

    @Override
    public ChatRoomDto editRoom(String roomId, ChatRoomEditInput param) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));

        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findByIdAndDeletedAtIsNull(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.getOwner().equals(user)) {
            throw new ChatRoomException(NOT_CHAT_ROOM_OWNER);
        }

        if(param.getEndDate().isBefore(param.getStartDate())){
            throw new ChatRoomException(TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE);
        }

        if(param.getMaxParticipant()<chatRoom.getParticipants().size()){
            throw new ChatRoomException(MAX_PARTICIPANT_IS_TOO_SMALL);
        }

        chatRoom.update(param);
        redisTemplateRepository.updateExpireTime(roomId,param);

        return ChatRoomDto.of(chatRoom);
    }

    @Override
    public void deleteRoom(String roomId) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));

        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findByIdAndDeletedAtIsNull(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.getOwner().equals(user)) {
            throw new ChatRoomException(NOT_CHAT_ROOM_OWNER);
        }

        chatRoom.delete();
        chatRoomParticipantRepository.deleteAllByChatRoomMySql(chatRoom);
    }


    @Override
    public ChatRoomServiceResult enterRoom(String roomId, String password) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));
        Long userId = user.getId();

        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findByIdAndDeletedAtIsNull(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        if(chatRoom.isPrivateRoom() && !chatRoom.getPassword().equals(password)){
            throw new ChatRoomException(PASSWORD_NOT_MATCH);
        }

        List<ChatRoomParticipant> participants = chatRoom.getParticipants();

        if(participants.stream().anyMatch(x->x.getUserId().equals(userId))){
            return new ChatRoomServiceResult(roomId,userId);
        } else if(participants.size() < chatRoom.getMaxParticipant()){

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
    public ChatRoomServiceResult leaveRoom(String roomId) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));
        Long userId = user.getId();

        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findByIdAndDeletedAtIsNull(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        ChatRoomParticipant participant =
                chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,userId)
                                .orElseThrow(()->new ChatRoomException(NOT_PARTICIPATED_USER));

        chatRoomParticipantRepository.delete(participant);
        return new ChatRoomServiceResult(roomId,userId);
    }

    @Override
    public MessagePagingResponse<MessageDto> getMessages(String roomId, LocalDate date,Integer page, Integer size) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));
        Long userId = user.getId();

        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findByIdAndDeletedAtIsNull(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        ChatRoomParticipant participant = chatRoomParticipantRepository
                .findByChatRoomMySqlAndUserId(chatRoom, userId).orElseThrow(
                        () -> new ChatRoomException(NOT_PARTICIPATED_USER));

        var leftTime = participant.getLeftAt();

        MessagePagingResponse<MessageDto> response = new MessagePagingResponse<>();
        response.setLastDay(leftTime.toLocalDate().isEqual(date));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String targetDateString = date.format(formatter);
        String dayOfMessageId = chatRoom.getId()+"#"+targetDateString;

        List<Message> messages = redisTemplateRepository.getMessage(dayOfMessageId,page,size);
        List<MessageDto>messageDtos = messages.stream().filter(message -> message.getCreatedAt().isAfter(leftTime))
                .map(MessageDto::of).collect(Collectors.toList());
        response.setFromList(messageDtos,size,date);
        return response;
    }

    @Override
    public MessagePagingResponse<MessageDto> getMessagesMongo(String roomId, Integer page, Integer size) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));
        Long userId = user.getId();

        ChatRoomMySql chatRoomMySql = chatRoomMySqlRepository.findByIdAndDeletedAtIsNull(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        ChatRoomParticipant participant = chatRoomParticipantRepository
                .findByChatRoomMySqlAndUserId(chatRoomMySql, userId).orElseThrow(
                        () -> new ChatRoomException(NOT_PARTICIPATED_USER));

        ChatRoom chatRoom = chatRoomMongoRepository.findById(chatRoomMySql.getId()).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        LocalDateTime leftTime = participant.getLeftAt();

        MessagePagingResponse<MessageDto> response = new MessagePagingResponse<>();
        LocalDate targetDate = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        int pastPage = 0;

        while(true){
            targetDate = targetDate.minusDays(1);

            if(targetDate.minusDays(1).isBefore(leftTime.toLocalDate())){
                response.setLastDay(true);
            }

            String targetDateString = targetDate.format(formatter);
            String dayOfMessageId = chatRoom.getChatRoomId()+"#"+targetDateString;
            Optional<DayOfMessages> dayOfMessages = chatRoom.getDayOfMessages()
                                    .stream().filter(x->x.getId().equals(dayOfMessageId)).findFirst();

            if(dayOfMessages.isEmpty() && response.isLastDay()){
                break;
            }else if(dayOfMessages.isEmpty()){
                continue;
            }

            int dayOfMessageCount = dayOfMessages.get().getMessagesCount();
            int addPage = dayOfMessageCount%size == 0?
                    dayOfMessages.get().getMessagesCount()/size
                    : dayOfMessages.get().getMessagesCount()/size+1;

            if(pastPage+addPage>=page){
                break;
            }else {
                pastPage += addPage;
            }

        }

        PageRequest pageRequest = PageRequest.of(page-pastPage,size);
        Page<Message> messagePage= messageRepository.findAllByRoomId(roomId, pageRequest);
        Page<MessageDto> messageDtoPage = messagePage.map(MessageDto::of);
        response.setFromPage(messageDtoPage,targetDate);

        return response;
    }

    @Override
    public ChatRoomParticipantDto chatEnd(String roomId) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));
        Long userId = user.getId();

        ChatRoomMySql chatRoom = chatRoomMySqlRepository.findByIdAndDeletedAtIsNull(roomId).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        ChatRoomParticipant participant = chatRoomParticipantRepository
                .findByChatRoomMySqlAndUserId(chatRoom, userId).orElseThrow(
                        () -> new ChatRoomException(NOT_PARTICIPATED_USER));

        participant.setLeftAt(LocalDateTime.now());
        return ChatRoomParticipantDto.of(participant);
    }
}
