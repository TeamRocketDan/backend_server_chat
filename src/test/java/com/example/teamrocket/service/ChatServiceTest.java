package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomEditInput;
import com.example.teamrocket.chatRoom.entity.ChatRoom;
import com.example.teamrocket.chatRoom.entity.Message;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.mongo.ChatRoomMongoRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.teamrocket.error.type.ChatRoomErrorCode.*;
import static com.example.teamrocket.error.type.UserErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
                .privateRoom(false)
                .password("1234")
                .rcate1("rcate1")
                .rcate2("rcate2")
                .longitude("위도")
                .latitude("경도")
                .build();

        given(chatRoomMongoRepository.save(any())).willReturn(ChatRoom.builder().chatRoomId("1번방").build());
        given(chatRoomMySqlRepository.save(any())).willReturn(new ChatRoomMySql());
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        ArgumentCaptor<ChatRoomMySql> captor = ArgumentCaptor.forClass(ChatRoomMySql.class);
        //when
        chatService.createRoom(input);

        //then
        verify(chatRoomMySqlRepository,times(1)).save(captor.capture());
        ChatRoomMySql chatRoomMySqlCaptured = captor.getValue();
        assertEquals(1L,chatRoomMySqlCaptured.getOwner().getId());
        assertEquals("채팅방1",chatRoomMySqlCaptured.getTitle());
        assertEquals(8,chatRoomMySqlCaptured.getMaxParticipant());


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
                .privateRoom(false)
                .password("1234")
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
                .findAllByRcate1AndRcate2AndPrivateRoomFalseAndDeletedAtIsNullOrderByStartDate("서울시","동작구",pageRequest))
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
    void editRoomSuccess() {

        //given
        ChatRoomEditInput input = ChatRoomEditInput.builder()
                .title("채팅방1")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(4))
                .maxParticipant(8)
                .password("1234")
                .privateRoom(false)
                .build();

        User user = User.builder().id(1L).build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.of(ChatRoomMySql.builder().owner(user).participants(new ArrayList<>()).build()));

        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(user));


        //when
        var result = chatService.editRoom("1번방",input);

        //then

        assertEquals("채팅방1",result.getTitle());
        assertEquals(8,result.getMaxParticipant());
        assertFalse(result.isPrivateRoom());
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

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
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

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
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
    void editRoomFail_StartDateError() {

        //given
        ChatRoomEditInput input = ChatRoomEditInput.builder()
                .title("채팅방1")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(4))
                .maxParticipant(8)
                .password("1234")
                .privateRoom(false)
                .build();

        User user = User.builder().id(1L).build();
        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.of(ChatRoomMySql.builder().owner(user).build()));
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(user));
        //when
        //then
        try{
            chatService.editRoom("1번방",input);
        }catch (RuntimeException e){
            assertEquals(START_DATE_MUST_BE_AFTER_TODAY.getMessage(),e.getMessage());
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
                .password("1234")
                .privateRoom(false)
                .build();

        User user = User.builder().id(1L).build();
        List<ChatRoomParticipant> participants = new ArrayList<>();
        participants.add(new ChatRoomParticipant());
        participants.add(new ChatRoomParticipant());
        ChatRoomMySql chatRoom = ChatRoomMySql.builder().participants(participants).owner(user).build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
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
        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
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
        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
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
        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
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
    void enterRoomSuccess() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(3L).build()));

        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").participants(Collections.emptyList())
                .maxParticipant(3)
                .password("1234").build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.of(chatRoom));

        ArgumentCaptor<ChatRoomParticipant> captor = ArgumentCaptor.forClass(ChatRoomParticipant.class);
        //when
        chatService.enterRoom("1번방","1234");
        //then
        verify(chatRoomParticipantRepository,times(1)).save(captor.capture());
        ChatRoomParticipant chatRoomParticipantCaptured = captor.getValue();
        assertEquals(chatRoom,chatRoomParticipantCaptured.getChatRoomMySql());
        assertEquals(3L,chatRoomParticipantCaptured.getUserId());
    }

    @Test
    void enterRoomFail_ExceedMaxParticipant() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        List<ChatRoomParticipant> participants = new ArrayList<>();

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder().userId(1L)
                .build();
        participants.add(participant1);

        ChatRoomParticipant participant2 = ChatRoomParticipant.builder().userId(2L)
                .build();
        participants.add(participant2);

        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").maxParticipant(2)
                .password("1234").participants(participants).build();


        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.of(chatRoom));

        //when
        //then
        try{
            chatService.enterRoom("1번방","1234");
        }catch (Exception e){
            assertEquals(EXCEED_MAX_PARTICIPANTS.getMessage(),e.getMessage());
        }
    }

    @Test
    void enterRoomFail_PasswordNotMatch() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").privateRoom(true).maxParticipant(3)
                .password("1234").build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.of(chatRoom));

        //when
        //then
        try{
            chatService.enterRoom("1번방","4321");
        }catch (Exception e){
            assertEquals(PASSWORD_NOT_MATCH.getMessage(),e.getMessage());
        }
    }

    @Test
    void leaveRoomSuccess() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").maxParticipant(3)
                .password("1234").build();


        ChatRoomParticipant participant1 = ChatRoomParticipant.builder().userId(1L)
                .chatRoomMySql(chatRoom).build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,1L))
                .willReturn(Optional.of(participant1));

        ArgumentCaptor<ChatRoomParticipant> captor = ArgumentCaptor.forClass(ChatRoomParticipant.class);

        //when
        chatService.leaveRoom("1번방");

        //then
        verify(chatRoomParticipantRepository,times(1)).delete(captor.capture());
        ChatRoomParticipant capturedChatRoomParticipant = captor.getValue();
        assertEquals(1L,capturedChatRoomParticipant.getUserId());
        assertEquals("1번방",capturedChatRoomParticipant.getChatRoomMySql().getId());
    }

    @Test
    void leaveRoomFail_NoRoom() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
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

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
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

    @Test
    void getMessagesSuccess() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").build();
        
        ChatRoomParticipant participant1 = ChatRoomParticipant.builder()
                .userId(1L).leftAt(LocalDateTime.now().minusHours(1)).chatRoomMySql(chatRoomMySql).build();

        List<Message> messages = new ArrayList<>();

        Message message1 = new Message();
        message1.updateMessage("1번 메시지");
        message1.updateRoomIdAndCreatedAt("1번방");
        messages.add(message1);

        Message message2 = new Message();
        message2.updateMessage("2번 메시지");
        message2.updateRoomIdAndCreatedAt("1번방");
        messages.add(message2);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.of(chatRoomMySql));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,1L))
                .willReturn(Optional.of(participant1));

        given(redisTemplateRepository
                .getMessage("1번방#"+LocalDate.now().format(formatter),0,5)).willReturn(messages);


        //when
        var results = chatService.getMessages("1번방",LocalDate.now(),0,5);

        //then
        assertEquals(LocalDate.now(),results.getTargetDay());
        assertEquals(5,results.getSize());
        assertTrue(results.isLastDay());
        assertTrue(results.isLastPage());
        assertEquals(2,results.getContent().size());
        assertEquals("1번 메시지",results.getContent().get(0).getMessage());
        assertEquals("2번 메시지",results.getContent().get(1).getMessage());
    }

    @Test
    void getMessagesFail_NoRoom() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.empty());

        //when
        //then
        try{
            chatService.getMessages("1번방", LocalDate.now(),10,10);
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

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.of(chatRoomMySql));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,1L))
                .willReturn(Optional.empty());


        //when
        //then
        try{
            chatService.getMessages("1번방", LocalDate.now(),10,10);
        }catch (Exception e){
            assertEquals(NOT_PARTICIPATED_USER.getMessage(),e.getMessage());
        }
    }

    @Test
    void chatEndSuccess() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").build();

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder()
                .userId(1L).chatRoomMySql(chatRoomMySql).build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.of(chatRoomMySql));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,1L))
                .willReturn(Optional.of(participant1));


        //when
        var result = chatService.chatEnd("1번방");

        //then
        assertNotNull(result.getLeftAt());
    }

    @Test
    void chatEndFail_NoChatRoom() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.empty());
        
        //when
        //then
        try{
            chatService.chatEnd("1번방");
        }catch (Exception e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }

    }

    @Test
    void chatEndFail_NotParticipated() {
        //given
        given(commonRequestContext.getMemberUuId()).willReturn("uuid");
        given(userRepository.findByUuid("uuid")).willReturn(Optional.of(User.builder().id(1L).build()));

        ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").build();

        given(chatRoomMySqlRepository.findByIdAndDeletedAtIsNull("1번방")).willReturn(
                Optional.of(chatRoomMySql));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,1L))
                .willReturn(Optional.empty());


        //when
        //then
        try{
            chatService.chatEnd("1번방");
        }catch (Exception e){
            assertEquals(NOT_PARTICIPATED_USER.getMessage(),e.getMessage());
        }
    }

}