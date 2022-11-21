package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomEditInput;
import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.mongo.ChatRoomMongoRepository;
import com.example.teamrocket.chatRoom.repository.mongo.MessageRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.mysql.ChatRoomParticipantRepository;
import com.example.teamrocket.chatRoom.repository.redis.RedisTemplateRepository;
import com.example.teamrocket.error.exception.UserException;
import com.example.teamrocket.user.entity.User;
import com.example.teamrocket.user.repository.UserRepository;
import com.example.teamrocket.utils.CommonRequestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.teamrocket.error.type.ChatRoomErrorCode.*;
import static com.example.teamrocket.error.type.UserErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatRoomMySqlRepository chatRoomMySqlRepository;

    @Mock
    private ChatRoomMongoRepository chatRoomMongoRepository;

    @Mock
    private ChatRoomParticipantRepository chatRoomParticipantRepository;

    @Mock
    private CommonRequestContext commonRequestContext;

    @Mock
    private RedisTemplateRepository redisTemplateRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    void createRoomSuccess() {
        //given
        ChatRoomCreateInput input = ChatRoomCreateInput.builder()
                .title("채팅방1")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .maxParticipant(8)
                .rcate1("rcate1")
                .rcate2("rcate2")
                .longitude("위도")
                .latitude("경도")
                .build();

        given(chatRoomMongoRepository.save(any())).willReturn(ChatRoom.builder().chatRoomId("1번방").build());
        given(chatRoomMySqlRepository.save(any())).willReturn(new ChatRoomMySql());
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        ArgumentCaptor<ChatRoomMySql> roomCaptor = ArgumentCaptor.forClass(ChatRoomMySql.class);
        ArgumentCaptor<ChatRoomParticipant> participantCaptor = ArgumentCaptor.forClass(ChatRoomParticipant.class);
        //when
        chatService.createRoom(input);

        //then
        verify(chatRoomMySqlRepository,times(1)).save(roomCaptor.capture());
        ChatRoomMySql chatRoomMySqlCaptured = roomCaptor.getValue();
        assertEquals(1L,chatRoomMySqlCaptured.getOwner().getId());
        assertEquals("채팅방1",chatRoomMySqlCaptured.getTitle());
        assertEquals(8,chatRoomMySqlCaptured.getMaxParticipant());

        verify(chatRoomParticipantRepository,times(1)).save(participantCaptor.capture());
        ChatRoomParticipant chatRoomParticipantCaptured = participantCaptor.getValue();
        assertEquals(chatRoomMySqlCaptured,chatRoomParticipantCaptured.getChatRoomMySql());
        assertEquals(1L,chatRoomParticipantCaptured.getUser().getId());
        assertTrue(chatRoomParticipantCaptured.isOwner());

    }


    @Test
    void createRoomFail_NoUser() {
        //given
        ChatRoomCreateInput input = ChatRoomCreateInput.builder()
                .build();

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.empty());

        //when
        //then
        try{
            chatService.createRoom(input);
        } catch (UserException e){
            assertEquals(USER_NOT_FOUND.getMessage(),e.getMessage());
        }
    }

    @Test
    void createRoomFail_TravelDateIssue() {
        //given
        ChatRoomCreateInput input = ChatRoomCreateInput.builder()
                .title("채팅방1")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().minusDays(1))
                .maxParticipant(8)
                .rcate1("rcate1")
                .rcate2("rcate2")
                .longitude("위도")
                .latitude("경도")
                .build();
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        //when
        //then
        try{
            chatService.createRoom(input);
        } catch (Exception e){
            assertEquals(TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE.getMessage(),e.getMessage());
        }
    }


    @Test
    void listRoomSuccess() {
        //given
        List<ChatRoomMySql> roomLists = new ArrayList<>();

        User user1 = User.builder().profileImage("프로필 이미지 경로1").nickname("닉네임1").build();
        User user2 = User.builder().profileImage("프로필 이미지 경로2").nickname("닉네임2").build();
        User user3 = User.builder().profileImage("프로필 이미지 경로3").nickname("닉네임3").build();

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder().build();
        List<ChatRoomParticipant> list = new ArrayList<>();
        list.add(participant1);

        ChatRoomMySql room1 = ChatRoomMySql.builder()
                .title("채팅방1").rcate1("서울시").owner(user1).participants(new ArrayList<>()).build();
        ChatRoomMySql room2 = ChatRoomMySql.builder()
                .title("채팅방2").rcate1("서울시").owner(user2).participants(list).build();
        ChatRoomMySql room3 = ChatRoomMySql.builder()
                .title("채팅방3").rcate1("서울시").owner(user3).participants(new ArrayList<>()).build();

        roomLists.add(room1);
        roomLists.add(room2);
        roomLists.add(room3);

        PageRequest pageRequest = PageRequest.of(0,10);
        Page<ChatRoomMySql> chatRoomMySqlPage = new PageImpl<>(roomLists);


        given(chatRoomMySqlRepository
                .findAllByRcate1AndRcate2AndDeletedAtIsNullAndEndDateAfterOrderByStartDate(eq("서울시"),eq("동작구"),any(),eq(pageRequest)))
                .willReturn(chatRoomMySqlPage);

        //when
        var results = chatService.listRoom("서울시","동작구",pageRequest);
        //then
        assertEquals(3,results.getSize());

        assertEquals("채팅방1",results.getContent().get(0).getTitle());
        assertEquals(0,results.getContent().get(0).getCurParticipant());
        assertEquals("프로필 이미지 경로1",results.getContent().get(0).getOwnerProfileImage());
        assertEquals("닉네임1",results.getContent().get(0).getOwnerNickName());

        assertEquals("채팅방2",results.getContent().get(1).getTitle());
        assertEquals(1,results.getContent().get(1).getCurParticipant());
        assertEquals("프로필 이미지 경로2",results.getContent().get(1).getOwnerProfileImage());
        assertEquals("닉네임2",results.getContent().get(1).getOwnerNickName());
    }

    @Test
    void listRoomSuccess_NoSearchCondition() {
        //given
        List<ChatRoomMySql> roomLists = new ArrayList<>();

        User user1 = User.builder().profileImage("프로필 이미지 경로1").nickname("닉네임1").build();
        User user2 = User.builder().profileImage("프로필 이미지 경로2").nickname("닉네임2").build();
        User user3 = User.builder().profileImage("프로필 이미지 경로3").nickname("닉네임3").build();

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder().build();
        List<ChatRoomParticipant> list = new ArrayList<>();
        list.add(participant1);

        ChatRoomMySql room1 = ChatRoomMySql.builder()
                .title("채팅방1").rcate1("서울시").owner(user1).participants(new ArrayList<>()).build();
        ChatRoomMySql room2 = ChatRoomMySql.builder()
                .title("채팅방2").rcate1("서울시").owner(user2).participants(list).build();
        ChatRoomMySql room3 = ChatRoomMySql.builder()
                .title("채팅방3").rcate1("서울시").owner(user3).participants(new ArrayList<>()).build();

        roomLists.add(room1);
        roomLists.add(room2);
        roomLists.add(room3);

        PageRequest pageRequest = PageRequest.of(0,10);
        Page<ChatRoomMySql> chatRoomMySqlPage = new PageImpl<>(roomLists);


        given(chatRoomMySqlRepository
                .findAllByDeletedAtIsNullAndEndDateAfterOrderByStartDate(any(),eq(pageRequest)))
                .willReturn(chatRoomMySqlPage);

        //when
        var results = chatService.listRoom(null,null,pageRequest);
        //then
        assertEquals(3,results.getSize());

        assertEquals("채팅방1",results.getContent().get(0).getTitle());
        assertEquals(0,results.getContent().get(0).getCurParticipant());
        assertEquals("프로필 이미지 경로1",results.getContent().get(0).getOwnerProfileImage());
        assertEquals("닉네임1",results.getContent().get(0).getOwnerNickName());

        assertEquals("채팅방2",results.getContent().get(1).getTitle());
        assertEquals(1,results.getContent().get(1).getCurParticipant());
        assertEquals("프로필 이미지 경로2",results.getContent().get(1).getOwnerProfileImage());
        assertEquals("닉네임2",results.getContent().get(1).getOwnerNickName());
    }

    @Test
    void listRoomFail_invalidSearchCondition() {
        //given
        PageRequest pageRequest = PageRequest.of(0,10);
        //when
        //then
        try{
            chatService.listRoom("Hi",null,pageRequest);
        }catch (Exception e){
            assertEquals(INVALID_SEARCH_CONDITION.getMessage(),e.getMessage());
        }
    }

    @Test
    void myListRoomSuccess() {
        //given
        User user1 = User.builder().id(1L).profileImage("프로필 이미지 경로1").nickname("닉네임1").build();
        ChatRoomParticipant participant1 = ChatRoomParticipant.builder().user(user1)
                .lastMessageTime(LocalDateTime.now().minusHours(2)).build();

        ChatRoomMySql room1 = ChatRoomMySql.builder()
                .title("채팅방1").rcate1("서울시").owner(user1).endDate(LocalDate.now().plusDays(3))
                .participants(Arrays.asList(participant1)).build();
        ChatRoomMySql room2 = ChatRoomMySql.builder()
                .title("채팅방2").rcate1("서울시").owner(user1).endDate(LocalDate.now().plusDays(3))
                .participants(Arrays.asList(participant1)).build();
        ChatRoomMySql room3 = ChatRoomMySql.builder()
                .title("채팅방3").rcate1("서울시").owner(user1).endDate(LocalDate.now().plusDays(3))
                .participants(Arrays.asList(participant1)).build();

        List<ChatRoomMySql> rooms = new ArrayList<>();

        rooms.add(room1);
        rooms.add(room2);
        rooms.add(room3);

        PageRequest pageRequest = PageRequest.of(0,10);
        Page<ChatRoomMySql> chatRoomPage = new PageImpl<>(rooms);

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(user1));
        given(chatRoomMySqlRepository
                .findAllByUserIdAndAndDeletedAtIsNullAndEndDateAfterOrderByStartDate(user1, LocalDate.now().minusDays(1),pageRequest))
                .willReturn(chatRoomPage);

        given(redisTemplateRepository.getMessageSize(any()))
                .willReturn(1L);
        given(redisTemplateRepository.getMessage(any(), eq(0), eq(1)))
                .willReturn(Arrays.asList(Message.builder().createdAt(LocalDateTime.now().minusHours(1)).build()));


        //when
        var results = chatService.myListRoom(pageRequest);
        //then
        assertEquals(3,results.getSize());

        assertEquals("채팅방1",results.getContent().get(0).getTitle());
        assertEquals(1,results.getContent().get(0).getCurParticipant());
        assertEquals("프로필 이미지 경로1",results.getContent().get(0).getOwnerProfileImage());
        assertEquals("닉네임1",results.getContent().get(0).getOwnerNickName());
        assertTrue(results.getContent().get(0).isNewMessage());

        assertEquals("채팅방2",results.getContent().get(1).getTitle());
        assertEquals(1,results.getContent().get(1).getCurParticipant());
        assertEquals("프로필 이미지 경로1",results.getContent().get(1).getOwnerProfileImage());
        assertEquals("닉네임1",results.getContent().get(1).getOwnerNickName());
        assertTrue(results.getContent().get(1).isNewMessage());
    }

    @Test
    void myListRoomFail_NoUser() {
        //given

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.empty());

        //when
        //then
        try{
            PageRequest pageRequest = PageRequest.of(0,10);
            chatService.myListRoom(pageRequest);
        } catch (UserException e){
            assertEquals(USER_NOT_FOUND.getMessage(),e.getMessage());
        }
    }


    @Test
    void editRoomSuccess() {

        //given
        ChatRoomEditInput input = ChatRoomEditInput.builder()
                .title("채팅방1")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(4))
                .maxParticipant(8)
                .build();

        User user = User.builder().id(1L).build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(ChatRoomMySql.builder().owner(user).participants(new ArrayList<>()).build()));

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(user));


        //when
        var result = chatService.editRoom("1번방",input);

        //then

        assertEquals("채팅방1",result.getTitle());
        assertEquals(8,result.getMaxParticipant());
    }


    @Test
    void editRoomFail_NoUser() {

        //given
        ChatRoomEditInput input = new ChatRoomEditInput();

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.empty());

        //when
        //then
        try{
            chatService.editRoom("1번방",input);
        }catch (Exception e){
            assertEquals(USER_NOT_FOUND.getMessage(),e.getMessage());
        }

    }

    @Test
    void editRoomFail_NoRoom() {

        //given
        ChatRoomEditInput input = new ChatRoomEditInput();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.empty());
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        //when
        //then
        try{
            chatService.editRoom("1번방",input);
        }catch (Exception e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }

    }

    @Test
    void editRoomFail_NotOwnerUser() {

        //given
        ChatRoomEditInput input = new ChatRoomEditInput();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(ChatRoomMySql.builder().owner(User.builder().id(1L).build()).build()));

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(2L).build()));
        //when
        //then
        try{
            chatService.editRoom("1번방",input);
        }catch (Exception e){
            assertEquals(NOT_CHAT_ROOM_OWNER.getMessage(),e.getMessage());
        }
    }


    @Test
    void editRoomFail_TooSmallMaxParticipant() {

        //given
        ChatRoomEditInput input = ChatRoomEditInput.builder()
                .title("채팅방1")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(4))
                .maxParticipant(1)
                .build();

        User user = User.builder().id(1L).build();
        List<ChatRoomParticipant> participants = new ArrayList<>();
        participants.add(new ChatRoomParticipant());
        participants.add(new ChatRoomParticipant());
        ChatRoomMySql chatRoom = ChatRoomMySql.builder().participants(participants).owner(user).build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(chatRoom));
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(user));
        //when
        //then
        try{
            chatService.editRoom("1번방",input);
        }catch (RuntimeException e){
            assertEquals(MAX_PARTICIPANT_IS_TOO_SMALL.getMessage(),e.getMessage());
        }
    }


    @Test
    void deleteRoomSuccess() {
        //given

        User user = User.builder().id(1L).build();
        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(ChatRoomMySql.builder().owner(user).build()));

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(user));

        //when
        chatService.deleteRoom("1번방");

        //then

    }

    @Test
    void deleteRoomFail_NoUser() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.empty());
        //when
        //then
        try{
            chatService.deleteRoom("1번방");
        }catch (RuntimeException e){
            assertEquals(USER_NOT_FOUND.getMessage(),e.getMessage());
        }
    }


    @Test
    void deleteRoomFail_NoRoom() {
        //given
        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.empty());

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));
        //when
        //then
        try{
            chatService.deleteRoom("1번방");
        }catch (RuntimeException e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }
    }

    @Test
    void deleteRoomFail_NotOwnerUser() {
        //given
        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(ChatRoomMySql.builder().owner(User.builder().id(1L).build()).build()));

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(0L).build()));

        //when
        //then
        try{
            chatService.deleteRoom("1번방");
        }catch (RuntimeException e){
            assertEquals(NOT_CHAT_ROOM_OWNER.getMessage(),e.getMessage());
        }
    }

    @Test
    void enterRoomSuccess_newUser() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(3L).build()));

        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").participants(Collections.emptyList())
                .maxParticipant(3)
                .build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(chatRoom));

        ArgumentCaptor<ChatRoomParticipant> captor = ArgumentCaptor.forClass(ChatRoomParticipant.class);
        //when
        var result = chatService.enterRoom("1번방");
        //then
        verify(chatRoomParticipantRepository,times(1)).save(captor.capture());
        ChatRoomParticipant chatRoomParticipantCaptured = captor.getValue();
        assertEquals(chatRoom,chatRoomParticipantCaptured.getChatRoomMySql());
        assertEquals(3L,chatRoomParticipantCaptured.getUser().getId());
        assertFalse(chatRoomParticipantCaptured.isOwner());
        assertEquals(chatRoom.getId(),result.getChatRoomId());
        assertEquals(3L,result.getUserId());
        assertTrue(result.isNewUser());
    }

    @Test
    void enterRoomSuccess_oldUser() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        User user = User.builder().id(3L).build();
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(user));

        List<ChatRoomParticipant> participants = new ArrayList<>();
        participants.add(ChatRoomParticipant.builder().user(user).build());
        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").participants(participants)
                .maxParticipant(3)
                .build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(chatRoom));

        //when
        var result = chatService.enterRoom("1번방");
        //then

        assertEquals(chatRoom.getId(),result.getChatRoomId());
        assertEquals(3L,result.getUserId());
        assertFalse(result.isNewUser());
    }

    @Test
    void enterRoomFail_ExceedMaxParticipant() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        List<ChatRoomParticipant> participants = new ArrayList<>();

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder().user(User.builder().id(1L).build())
                .build();
        participants.add(participant1);

        ChatRoomParticipant participant2 = ChatRoomParticipant.builder().user(User.builder().id(1L).build())
                .build();
        participants.add(participant2);

        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").maxParticipant(2)
                .participants(participants).build();


        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(chatRoom));

        //when
        //then
        try{
            chatService.enterRoom("1번방");
        }catch (Exception e){
            assertEquals(EXCEED_MAX_PARTICIPANTS.getMessage(),e.getMessage());
        }
    }

    @Test
    void leaveRoomSuccess() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").maxParticipant(3)
                .build();


        ChatRoomParticipant participant1 = ChatRoomParticipant.builder().user(User.builder().id(1L).build())
                .chatRoomMySql(chatRoom).build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,1L))
                .willReturn(Optional.of(participant1));

        ArgumentCaptor<ChatRoomParticipant> captor = ArgumentCaptor.forClass(ChatRoomParticipant.class);

        //when
        chatService.leaveRoom("1번방");

        //then
        verify(chatRoomParticipantRepository,times(1)).delete(captor.capture());
        ChatRoomParticipant capturedChatRoomParticipant = captor.getValue();
        assertEquals(1L,capturedChatRoomParticipant.getUser().getId());
        assertEquals("1번방",capturedChatRoomParticipant.getChatRoomMySql().getId());
    }

    @Test
    void leaveRoomFail_NoRoom() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.empty());

        //when
        //then
        try{
            chatService.leaveRoom("1번방");
        } catch (Exception e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }
    }

    @Test
    void leaveRoomFail_NotParticipate() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(3L).build()));

        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(chatRoom));

        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,3L))
                .willReturn(Optional.empty());
        //when
        //then
        try{
            chatService.leaveRoom("1번방");
        } catch (Exception e){
            assertEquals(NOT_PARTICIPATED_USER.getMessage(),e.getMessage());
        }
    }

//    @Test createdAt 조작 불가
//    void getMessagesSuccess() {
//        //given
//        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
//        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));
//
//        ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").build();
//
//        ChatRoomParticipant participant1 = ChatRoomParticipant.builder()
//                .user(User.builder().id(1L).build()).chatRoomMySql(chatRoomMySql).build();
//
//        List<Message> messages = new ArrayList<>();
//
//        Message message1 = new Message();
//        message1.updateMessage("1번 메시지");
//        message1.updateRoomIdAndCreatedAt("1번방");
//        messages.add(message1);
//
//        Message message2 = new Message();
//        message2.updateMessage("2번 메시지");
//        message2.updateRoomIdAndCreatedAt("1번방");
//        messages.add(message2);
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//
//        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
//                Optional.of(chatRoomMySql));
//        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,1L))
//                .willReturn(Optional.of(participant1));
//
//        given(redisTemplateRepository
//                .getMessage("1번방#"+LocalDate.now().format(formatter),0,5)).willReturn(messages);
//
//
//        //when
//        var results = chatService.getMessages("1번방",0,5);
//
//        //then
//        assertEquals(LocalDate.now(),results.getTargetDay());
//        assertEquals(5,results.getSize());
//        assertTrue(results.isLastDay());
//        assertTrue(results.isLastPage());
//        assertEquals(2,results.getContent().size());
//        assertEquals("1번 메시지",results.getContent().get(0).getMessage());
//        assertEquals("2번 메시지",results.getContent().get(1).getMessage());
//    }

    @Test
    void getMessagesFail_NoRoom() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.empty());

        //when
        //then
        try{
            chatService.getMessages("1번방",10,10);
        }catch (Exception e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }
    }

    @Test
    void getMessagesFail_NotParticipate() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(chatRoomMySql));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,1L))
                .willReturn(Optional.empty());


        //when
        //then
        try{
            chatService.getMessages("1번방", 10,10);
        }catch (Exception e){
            assertEquals(NOT_PARTICIPATED_USER.getMessage(),e.getMessage());
        }
    }

    @Test
    void getRoomInfoSuccess() {
        //given

        List<ChatRoomParticipant> participants = new ArrayList<>();

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder()
                .user(User.builder().id(1L).build()).isOwner(true).build();
        participants.add(participant1);

        ChatRoomParticipant participant2 = ChatRoomParticipant.builder()
                .user(User.builder().id(2L).build()).isOwner(false).build();
        participants.add(participant2);

        ChatRoomParticipant participant3 = ChatRoomParticipant.builder()
                .user(User.builder().id(3L).build()).isOwner(false).build();
        participants.add(participant3);

        ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").title("1번방 제목").participants(participants).build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(chatRoomMySql));

        //when
        var result = chatService.getRoomInfo("1번방");

        //then
        assertEquals("1번방 제목", result.getRoomTitle());

        assertEquals(participants.size(),result.getParticipants().size());
        assertEquals(1L,result.getParticipants().get(0).getUserId());
        assertTrue(result.getParticipants().get(0).isOwner());

        assertEquals(2L,result.getParticipants().get(1).getUserId());
        assertFalse(result.getParticipants().get(1).isOwner());

        assertEquals(3L,result.getParticipants().get(2).getUserId());
        assertFalse(result.getParticipants().get(2).isOwner());
    }

    @Test
    void getRoomInfoFail_NoChatRoom() {
        //given
        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.empty());

        //when
        //then
        try{
            chatService.getRoomInfo("1번방");
        } catch (Exception e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }
    }

    @Test
    void chatEndSuccess() {
        //given
        User user1 = User.builder().id(1L).profileImage("프로필 이미지 경로1").nickname("닉네임1").build();

        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").participants(Collections.emptyList())
                .maxParticipant(3)
                .build();

        ChatRoomParticipant participant = ChatRoomParticipant.builder()
                .user(user1).chatRoomMySql(chatRoom).build();

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(user1));

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1))).willReturn(
                Optional.of(chatRoom));

        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,1L))
                .willReturn(Optional.of(participant));

        given(messageRepository.findFirstByRoomIdOrderByCreatedAtDesc(chatRoom.getId())).willReturn(
                Optional.empty());

        //when
        var result = chatService.chatEnd("1번방");

        //then
        assertEquals("1번방",result.getChatRoomId());
        assertEquals(1L,result.getUserId());
    }

    @Test
    void chatEndFail_NoUser() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(
                Optional.empty());

        //when
        //then
        try{
            chatService.chatEnd("1번방");
        } catch (Exception e){
            assertEquals(USER_NOT_FOUND.getMessage(),e.getMessage());
        }
    }

    @Test
    void chatEndFail_NoChatRoom() {
        //given
        User user1 = User.builder().id(1L).profileImage("프로필 이미지 경로1").nickname("닉네임1").build();

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(
                Optional.of(user1));

        given(chatRoomMySqlRepository
                .findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1)))
                .willReturn(Optional.empty());

        //when
        //then
        try{
            chatService.chatEnd("1번방");
        } catch (Exception e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }
    }

    @Test
    void chatEndFail_NotParticipated() {
        //given
        User user1 = User.builder().id(1L).profileImage("프로필 이미지 경로1").nickname("닉네임1").build();

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(
                Optional.of(user1));

        ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").build();

        given(chatRoomMySqlRepository
                .findByIdAndDeletedAtIsNullAndEndDateAfter("1번방",LocalDate.now().minusDays(1)))
                .willReturn(Optional.of(chatRoomMySql));

        given( chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,user1.getId()))
                .willReturn(Optional.empty());
        //when
        //then
        try{
            chatService.chatEnd("1번방");
        } catch (Exception e){
            assertEquals(NOT_PARTICIPATED_USER.getMessage(),e.getMessage());
        }
    }
}