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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
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
        ChatRoomDto result = ChatRoomDto.of(chatRoomMySqlRepository.save(chatRoom));

        ChatRoomParticipant participant = ChatRoomParticipant.builder()
                .chatRoomMySql(chatRoom)
                .isOwner(true)
                .user(user)
                .build();

        chatRoomParticipantRepository.save(participant);
        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public PagingResponse<ChatRoomDto> listRoom(String rcate1, String rcate2, Pageable pageRequest) {
        Page<ChatRoomMySql> chatRooms;

        if(ObjectUtils.isEmpty(rcate1) && ObjectUtils.isEmpty(rcate2)){
            chatRooms = chatRoomMySqlRepository.findAllByDeletedAtIsNullAndEndDateAfterOrderByStartDate(
                    LocalDate.now().minusDays(1),pageRequest);
        }else if(!ObjectUtils.isEmpty(rcate1) && !ObjectUtils.isEmpty(rcate2)){
            chatRooms = chatRoomMySqlRepository.findAllByRcate1AndRcate2AndDeletedAtIsNullAndEndDateAfterOrderByStartDate(
                    rcate1,rcate2,LocalDate.now().minusDays(1),pageRequest);
        }else{
            throw new ChatRoomException(INVALID_SEARCH_CONDITION);
        }

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

    @Transactional(readOnly = true)
    @Override
    public PagingResponse<ChatRoomDto> myListRoom(Pageable pageRequest) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));

        Page<ChatRoomMySql> chatRoomPage =
                chatRoomMySqlRepository
                        .findAllByUserIdAndAndDeletedAtIsNullAndEndDateAfterOrderByStartDate(user, LocalDate.now().minusDays(1),pageRequest);

        List<ChatRoomDto> contents = new ArrayList<>(chatRoomPage.getContent().size());
        for(ChatRoomMySql chatRoom:chatRoomPage.getContent()){
            ChatRoomDto chatRoomDto = ChatRoomDto.of(chatRoom);
            chatRoomDto.setCurParticipant(chatRoom.getParticipants().size());

            User owner = chatRoom.getOwner();
            chatRoomDto.setOwnerInfo(owner.getNickname(),owner.getProfileImage());
            contents.add(chatRoomDto);
        }

        PagingResponse<ChatRoomDto> result = PagingResponse.fromEntity(chatRoomPage);
        result.setContent(contents);

        return result;
    }

    @Override
    public ChatRoomDto editRoom(String roomId, ChatRoomEditInput param) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));

        ChatRoomMySql chatRoom = chatRoomMySqlRepository
                .findByIdAndDeletedAtIsNullAndEndDateAfter(roomId,LocalDate.now().minusDays(1))
                .orElseThrow(() -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

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

        ChatRoomMySql chatRoom = chatRoomMySqlRepository
                .findByIdAndDeletedAtIsNullAndEndDateAfter(roomId,LocalDate.now().minusDays(1))
                .orElseThrow(() -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.getOwner().equals(user)) {
            throw new ChatRoomException(NOT_CHAT_ROOM_OWNER);
        }

        chatRoom.delete();
        chatRoomParticipantRepository.deleteAllByChatRoomMySql(chatRoom);
    }

    @Override
    public ChatRoomEnterResult enterRoom(String roomId) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));

        ChatRoomMySql chatRoom = chatRoomMySqlRepository
                .findByIdAndDeletedAtIsNullAndEndDateAfter(roomId,LocalDate.now().minusDays(1))
                .orElseThrow(() -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        List<ChatRoomParticipant> participants = chatRoom.getParticipants();

        if(participants.stream().anyMatch(x->x.getUser().equals(user))){
            return new ChatRoomEnterResult(roomId,user.getId(),false);
        } else if(participants.size() < chatRoom.getMaxParticipant()){

            ChatRoomParticipant participant = ChatRoomParticipant.builder()
                    .isOwner(false)
                    .chatRoomMySql(chatRoom)
                    .user(user)
                    .build();
            chatRoomParticipantRepository.save(participant);
            return new ChatRoomEnterResult(roomId,user.getId(),true);
        } else{
            throw new ChatRoomException(EXCEED_MAX_PARTICIPANTS);
        }
    }

    @Override
    public ChatRoomServiceResult leaveRoom(String roomId) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));
        Long userId = user.getId();

        ChatRoomMySql chatRoom = chatRoomMySqlRepository
                .findByIdAndDeletedAtIsNullAndEndDateAfter(roomId,LocalDate.now().minusDays(1))
                .orElseThrow(() -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        ChatRoomParticipant participant =
                chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,userId)
                                .orElseThrow(()->new ChatRoomException(NOT_PARTICIPATED_USER));

        if(user.equals(chatRoom.getOwner())){
            chatRoom.delete();
            chatRoomParticipantRepository.deleteAllByChatRoomMySql(chatRoom);
        }else{
            chatRoomParticipantRepository.delete(participant);
        }
        return new ChatRoomServiceResult(roomId,userId);
    }

    @Transactional(readOnly = true)
    @Override
    public MessagePagingResponse<MessageDto> getMessages(String roomId, Integer page, Integer size) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));
        Long userId = user.getId();

        ChatRoomMySql chatRoomMySql = chatRoomMySqlRepository
                .findByIdAndDeletedAtIsNullAndEndDateAfter(roomId,LocalDate.now().minusDays(1))
                .orElseThrow(() -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        ChatRoomParticipant participant = chatRoomParticipantRepository
                .findByChatRoomMySqlAndUserId(chatRoomMySql, userId).orElseThrow(
                        () -> new ChatRoomException(NOT_PARTICIPATED_USER));

        MessagePagingResponse<MessageDto> response = new MessagePagingResponse<>();
        LocalDate targetDate = LocalDate.now().plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        int pastPage = 0;
        int addPage=0;
        int dayOfMessageCount=0;

        while(true){

            targetDate = targetDate.minusDays(1);

            if(targetDate.equals(participant.getCreatedAt().toLocalDate())){
                response.setLastDay(true);
            }

            String dayOfMessageId = chatRoomMySql.getId()+"#"+targetDate.format(formatter);

            if(targetDate.isAfter(LocalDate.now().minusDays(2))){
                dayOfMessageCount = redisTemplateRepository.getMessageSize(dayOfMessageId).intValue();

                if(dayOfMessageCount == 0 && response.isLastDay()){
                    break;
                }else if(dayOfMessageCount == 0){
                    continue;
                }
                addPage = dayOfMessageCount%size == 0? dayOfMessageCount/size : dayOfMessageCount/size+1;

                if(pastPage+addPage>=page){
                    break;
                }else {
                    pastPage += addPage;
                }

            } else{
                ChatRoom chatRoom = chatRoomMongoRepository.findById(chatRoomMySql.getId()).orElseThrow(
                        () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

                Optional<DayOfMessages> dayOfMessages = chatRoom.getDayOfMessages()
                        .stream().filter(x->x.getId().equals(dayOfMessageId)).findFirst();

                if(dayOfMessages.isEmpty() && response.isLastDay()){
                    break;
                }else if(dayOfMessages.isEmpty()){
                    continue;
                }

                dayOfMessageCount = dayOfMessages.get().getMessagesCount();
                addPage = dayOfMessageCount%size == 0? dayOfMessageCount/size : dayOfMessageCount/size+1;

                if(pastPage+addPage>=page){
                    break;
                }else {
                    pastPage += addPage;
                }
            }
        }

        if(targetDate.isAfter(LocalDate.now().minusDays(2))){
            String dayOfMessageId = chatRoomMySql.getId()+"#"+targetDate.format(formatter);
            List<Message> messages = redisTemplateRepository.getMessage(dayOfMessageId,page,size);
            List<MessageDto>messageDtos;
            if(response.isLastDay()){
                messageDtos = messages.stream().filter(message -> message.getCreatedAt().isAfter(participant.getCreatedAt()))
                        .map(MessageDto::of).collect(Collectors.toList());
            }else{
                messageDtos = messages.stream().map(MessageDto::of).collect(Collectors.toList());
            }
            response.setFromList(messageDtos,targetDate,pastPage == page,size,addPage,
                    dayOfMessageCount,page-pastPage);
        }else{
            PageRequest pageRequest = PageRequest.of(page-pastPage,size);
            Page<Message> messagePage;

            if(response.isLastDay()){
                messagePage = messageRepository.findAllByRoomIdAndCreatedAtAfter(roomId,participant.getCreatedAt(),pageRequest);
            }else{
                messagePage= messageRepository.findAllByRoomId(roomId, pageRequest);
            }

            Page<MessageDto> messageDtoPage = messagePage.map(MessageDto::of);
            response.setFromPage(messageDtoPage,targetDate);
        }

        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public RoomInfoDto getRoomInfo(String roomId) {
        ChatRoomMySql chatRoom = chatRoomMySqlRepository
                .findByIdAndDeletedAtIsNullAndEndDateAfter(roomId,LocalDate.now().minusDays(1))
                .orElseThrow(() -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        return new RoomInfoDto(chatRoom.getTitle(),chatRoom.getParticipants()
                .stream().map(ChatRoomParticipantDto::of).collect(Collectors.toList()));
    }
}
