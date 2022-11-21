package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.*;
import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.DayOfMessages;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.mongo.ChatRoomMongoRepository;
import com.example.teamrocket.chatRoom.repository.mongo.DayOfMessageRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.teamrocket.error.type.ChatRoomErrorCode.*;
import static com.example.teamrocket.error.type.UserErrorCode.USER_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService{

    private final DayOfMessageRepository dayOfMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomMySqlRepository chatRoomMySqlRepository;
    private final ChatRoomMongoRepository chatRoomMongoRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final RedisTemplateRepository redisTemplateRepository;
    private final MessageRepository messageRepository;
    private final CommonRequestContext commonRequestContext;

    @Transactional
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

            ChatRoomParticipant participant = chatRoom.getParticipants().stream()
                    .filter(x-> x.getUser().equals(user)).findFirst().orElseThrow(
                           ()-> new ChatRoomException(NOT_PARTICIPATED_USER));

            chatRoomDto.setNewMessage(checkNewMessage(chatRoom,participant));

            contents.add(chatRoomDto);
        }

        PagingResponse<ChatRoomDto> result = PagingResponse.fromEntity(chatRoomPage);
        result.setContent(contents);

        return result;
    }

    @Transactional
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

    @Transactional
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

    @Transactional
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

    @Transactional
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

    /**
     * getMessage의 경우 오늘과 어제의 메시지는 redis 에서,
     * 그 이전의 메시지들은 MongoDB에서 가져온다.
    **/
    @Transactional(readOnly = true)
    @Override
    public MessagePagingResponse<MessageDto> getMessages(String roomId, Integer page, Integer size) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));

        ChatRoomMySql chatRoomMySql = chatRoomMySqlRepository
                .findByIdAndDeletedAtIsNullAndEndDateAfter(roomId,LocalDate.now().minusDays(1))
                .orElseThrow(() -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        ChatRoomParticipant participant = chatRoomParticipantRepository
                .findByChatRoomMySqlAndUserId(chatRoomMySql, user.getId()).orElseThrow(
                        () -> new ChatRoomException(NOT_PARTICIPATED_USER));

        MessagePagingResponse<MessageDto> response = new MessagePagingResponse<>();
        LocalDate targetDate = LocalDate.now().plusDays(1);

        int pastPage = 0;
        int addPage;
        int dayOfMessageCount;

        while(targetDate.isAfter(LocalDate.now().minusDays(2))){

            targetDate = targetDate.minusDays(1);

            if(targetDate.equals(participant.getCreatedAt().toLocalDate())){
                response.setLastDay(true);
            }

            String dayOfMessageId = getDayOfMessageId(chatRoomMySql,targetDate);
            dayOfMessageCount = redisTemplateRepository.getMessageSize(dayOfMessageId).intValue();

            if (dayOfMessageCount == 0 && response.isLastDay()) {
                break;
            } else if (dayOfMessageCount == 0) {
                continue;
            }
            addPage = dayOfMessageCount % size == 0 ? dayOfMessageCount / size : dayOfMessageCount / size + 1;

            if (pastPage + addPage > page) {

                List<Message> messages = redisTemplateRepository.getMessage(dayOfMessageId,page - pastPage,size);
                List<MessageDto>messageDtos;
                if(response.isLastDay()){
                    messageDtos = messages.stream().filter(message -> message.getCreatedAt().isAfter(participant.getCreatedAt()))
                            .map(MessageDto::of).collect(Collectors.toList());
                }else{
                    messageDtos = messages.stream().map(MessageDto::of).collect(Collectors.toList());
                }
                response.setFromList(messageDtos,targetDate,pastPage == page,size,addPage,
                        dayOfMessageCount,page-pastPage);

                return response;
            } else {
                pastPage += addPage;
            }
        }

        ChatRoom chatRoom = chatRoomMongoRepository.findById(chatRoomMySql.getId()).orElseThrow(
                () -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        List<DayOfMessages> dayOfMessagesList = chatRoom.getDayOfMessages().stream().sorted(
                (x,y)-> Integer.parseInt(y.getId().split("#")[1]) - Integer.parseInt(x.getId().split("#")[1])
        ).collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        int dayOfMessagesIdx = 0;
        while(dayOfMessagesIdx<dayOfMessagesList.size()){
            DayOfMessages dayOfMessages= dayOfMessagesList.get(dayOfMessagesIdx);
            if(dayOfMessagesIdx+1 == dayOfMessagesList.size()){
                response.setLastDay(true);
            }

            targetDate = LocalDate.parse(dayOfMessages.getId(),formatter);
            dayOfMessageCount = dayOfMessages.getMessagesCount();
            if(dayOfMessageCount == 0){
                dayOfMessageCount = messageRepository.countByRoomIdAndCreatedAtBetween
                        (roomId,targetDate,targetDate.plusDays(1)).intValue();
                dayOfMessages.setMessagesCount(dayOfMessageCount);
                dayOfMessageRepository.save(dayOfMessages);
            }
            addPage = dayOfMessageCount%size == 0? dayOfMessageCount/size : dayOfMessageCount/size+1;

            if(pastPage+addPage>page){
                break;
            }else {
                pastPage += addPage;
                dayOfMessagesIdx++;
            }
        }

        PageRequest pageRequest = PageRequest.of(page-pastPage,size);
        Page<Message> messagePage;

        if(response.isLastDay()){
            messagePage = messageRepository.findAllByRoomIdAndCreatedAtBetween(roomId,participant.getCreatedAt(),
                    targetDate.plusDays(1).atTime(0,0),pageRequest);
        }else{
            messagePage= messageRepository.findAllByRoomIdAndCreatedAtBetween(roomId, targetDate.atTime(0,0),
                    targetDate.plusDays(1).atTime(0,0),pageRequest);
        }

        Page<MessageDto> messageDtoPage = messagePage.map(MessageDto::of);
        response.setFromPage(messageDtoPage,targetDate);

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

    @Transactional
    @Override
    public ChatRoomServiceResult chatEnd(String roomId) {
        User user = userRepository.findByUuid(commonRequestContext.getMemberUuId()).orElseThrow(
                ()->new UserException(USER_NOT_FOUND));
        Long userId = user.getId();

        ChatRoomMySql chatRoom = chatRoomMySqlRepository
                .findByIdAndDeletedAtIsNullAndEndDateAfter(roomId,LocalDate.now().minusDays(1))
                .orElseThrow(() -> new ChatRoomException(CHAT_ROOM_NOT_FOUND));

        ChatRoomParticipant participant =
                chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,userId)
                        .orElseThrow(()->new ChatRoomException(NOT_PARTICIPATED_USER));

        Optional<Message> lastMessage = getLastMessage(chatRoom);
        if(lastMessage.isEmpty()){
            participant.setLastMessageTime(LocalDateTime.now());
        }else{
            participant.setLastMessageTime(lastMessage.get().getCreatedAt());
        }

        return new ChatRoomServiceResult(roomId,userId);
    }

    private String getDayOfMessageId(ChatRoomMySql room ,LocalDate date){
        return room.getId()+"#"+date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private boolean checkNewMessage(ChatRoomMySql chatRoom, ChatRoomParticipant participant) {
        LocalDateTime endTime = participant.getLastMessageTime() == null ? LocalDateTime.now():participant.getLastMessageTime();
        Optional<Message> latestMessage = getLastMessage(chatRoom);

        return latestMessage.isPresent() && latestMessage.get().getCreatedAt().isAfter(endTime);
    }

    private Optional<Message> getLastMessage(ChatRoomMySql chatRoom) {

        LocalDate targetDate = LocalDate.now().plusDays(1);
        while (targetDate.isAfter(LocalDate.now().minusDays(2))) {
            targetDate = targetDate.minusDays(1);
            String dayOfMessageId = getDayOfMessageId(chatRoom, LocalDate.now());
            Long messageCount = redisTemplateRepository.getMessageSize(dayOfMessageId);
            if (messageCount != 0L) {
                return Optional.of(redisTemplateRepository.getMessage(dayOfMessageId, 0, 1).get(0));
            }
        }

        return messageRepository.findFirstByRoomIdOrderByCreatedAtDesc(chatRoom.getId());
    }
}
