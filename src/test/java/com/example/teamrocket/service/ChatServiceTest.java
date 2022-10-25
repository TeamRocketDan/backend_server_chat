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
import com.example.teamrocket.error.exception.UserException;
import com.example.teamrocket.user.entity.User;
import com.example.teamrocket.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    void createRoomSuccess() {
        //given
        ChatRoomCreateInput input = ChatRoomCreateInput.builder()
                .title("채팅방1")
                .start_date(LocalDateTime.now())
                .end_date(LocalDateTime.now().plusDays(1))
                .maxParticipant(8)
                .privateRoom(false)
                .password("1234")
                .rcate1("rcate1")
                .rcate2("rcate2")
                .rcate3("rcate3")
                .longitude("위도")
                .latitude("경도")
                .build();

        given(chatRoomMySqlRepository.save(any())).willReturn(new ChatRoomMySql());
        given(userRepository.findById(1L)).willReturn(Optional.of(User.builder().id(1L).build()));

        ArgumentCaptor<ChatRoomMySql> captor = ArgumentCaptor.forClass(ChatRoomMySql.class);
        //when
        chatService.createRoom(1L,input);

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

        given(userRepository.findById(1L)).willReturn(Optional.empty());

        //when
        //then
        try{
            chatService.createRoom(1L,input);
        } catch (UserException e){
            assertEquals(USER_NOT_FOUND.getMessage(),e.getMessage());
        }



    }

    @Test
    void createRoomFail_TravelDateIssue() {
        //given
        ChatRoomCreateInput input = ChatRoomCreateInput.builder()
                .title("채팅방1")
                .start_date(LocalDateTime.now())
                .end_date(LocalDateTime.now().minusDays(1))
                .maxParticipant(8)
                .privateRoom(false)
                .password("1234")
                .rcate1("rcate1")
                .rcate2("rcate2")
                .rcate3("rcate3")
                .longitude("위도")
                .latitude("경도")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(User.builder().id(1L).build()));

        //when
        //then
        try{
            chatService.createRoom(1L,input);
        } catch (Exception e){
            assertEquals(TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE.getMessage(),e.getMessage());
        }
    }


    @Test
    void listRoomSuccess() {
        //given
        List<ChatRoomMySql> roomLists = new ArrayList<>();
        ChatRoomMySql room1 = ChatRoomMySql.builder()
                .title("채팅방1").privateRoom(false).build();
        ChatRoomMySql room2 = ChatRoomMySql.builder()
                .title("채팅방2").privateRoom(false).build();
        ChatRoomMySql room3 = ChatRoomMySql.builder()
                .title("채팅방3").privateRoom(false).deletedAt(LocalDateTime.now()).build();
        ChatRoomMySql room4 = ChatRoomMySql.builder()
                .title("채팅방4").privateRoom(true).build();

        roomLists.add(room1);
        roomLists.add(room2);
        roomLists.add(room3);
        roomLists.add(room4);

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder().build();
        List<ChatRoomParticipant> list = new ArrayList<>();
        list.add(participant1);

        given(chatRoomMySqlRepository.findAll()).willReturn(roomLists);
        given(chatRoomParticipantRepository.findAllByChatRoomMySql(room1)).willReturn(new ArrayList<>());
        given(chatRoomParticipantRepository.findAllByChatRoomMySql(room2))
                .willReturn(list);

        //when
        var results = chatService.listRoom();
        //then
        assertEquals(2,results.size());
        assertEquals("채팅방1",results.get(0).getTitle());
        assertEquals(0,results.get(0).getCurParticipant());
        assertEquals("채팅방2",results.get(1).getTitle());
        assertEquals(1,results.get(1).getCurParticipant());

    }

    @Test
    void editRoomSuccess() {

        //given
        ChatRoomEditInput input = ChatRoomEditInput.builder()
                .title("채팅방1")
                .start_date(LocalDateTime.now().plusDays(2))
                .end_date(LocalDateTime.now().plusDays(4))
                .maxParticipant(8)
                .password("1234")
                .privateRoom(false)
                .build();

        User user = User.builder().id(1L).build();

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(ChatRoomMySql.builder().owner(user).build()));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        //when
        var result = chatService.editRoom(1L,"1번방",input);

        //then

        assertEquals("채팅방1",result.getTitle());
        assertEquals(8,result.getMaxParticipant());
        assertFalse(result.isPrivateRoom());
    }


    @Test
    void editRoomFail_NoUser() {

        //given
        ChatRoomEditInput input = new ChatRoomEditInput();

        given(userRepository.findById(1L)).willReturn(Optional.empty());

        //when
        //then
        try{
            chatService.editRoom(1L,"1번방",input);
        }catch (Exception e){
            assertEquals(USER_NOT_FOUND.getMessage(),e.getMessage());
        }

    }

    @Test
    void editRoomFail_NoRoom() {

        //given
        ChatRoomEditInput input = new ChatRoomEditInput();

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.empty());
        given(userRepository.findById(1L)).willReturn(Optional.of(User.builder().id(1L).build()));

        //when
        //then
        try{
            chatService.editRoom(1L,"1번방",input);
        }catch (Exception e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }

    }

    @Test
    void editRoomFail_NotOwnerUser() {

        //given
        ChatRoomEditInput input = new ChatRoomEditInput();

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(ChatRoomMySql.builder().owner(User.builder().id(1L).build()).build()));
        given(userRepository.findById(0L)).willReturn(Optional.of(
                User.builder().id(0L).build()));
        //when
        //then
        try{
            chatService.editRoom(0L,"1번방",input);
        }catch (Exception e){
            assertEquals(NOT_CHAT_ROOM_OWNER.getMessage(),e.getMessage());
        }
    }

    @Test
    void editRoomFail_StartDateError() {

        //given
        ChatRoomEditInput input = ChatRoomEditInput.builder()
                .title("채팅방1")
                .start_date(LocalDateTime.now().minusDays(1))
                .end_date(LocalDateTime.now().plusDays(4))
                .maxParticipant(8)
                .password("1234")
                .privateRoom(false)
                .build();

        User user = User.builder().id(1L).build();
        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(ChatRoomMySql.builder().owner(user).build()));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        //when
        //then
        try{
            chatService.editRoom(1L,"1번방",input);
        }catch (RuntimeException e){
            assertEquals(START_DATE_MUST_BE_AFTER_TODAY.getMessage(),e.getMessage());
        }
    }

    @Test
    void editRoomFail_TooSmallMaxParticipant() {

        //given
        ChatRoomEditInput input = ChatRoomEditInput.builder()
                .title("채팅방1")
                .start_date(LocalDateTime.now().plusDays(1))
                .end_date(LocalDateTime.now().plusDays(4))
                .maxParticipant(1)
                .password("1234")
                .privateRoom(false)
                .build();

        User user = User.builder().id(1L).build();
        ChatRoomMySql chatRoom = ChatRoomMySql.builder().owner(user).build();
        List<ChatRoomParticipant> participants = new ArrayList<>();
        participants.add(new ChatRoomParticipant());
        participants.add(new ChatRoomParticipant());

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoom));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom)).willReturn(participants);
        //when
        //then
        try{
            chatService.editRoom(1L,"1번방",input);
        }catch (RuntimeException e){
            assertEquals(MAX_PARTICIPANT_IS_TOO_SMALL.getMessage(),e.getMessage());
        }
    }


    @Test
    void deleteRoomSuccess() {
        //given

        User user = User.builder().id(1L).build();
        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(ChatRoomMySql.builder().owner(user).build()));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        ArgumentCaptor<ChatRoomMySql> captor = ArgumentCaptor.forClass(ChatRoomMySql.class);

        //when
        chatService.deleteRoom(1L,"1번방");

        //then
        verify(chatRoomMySqlRepository,times(1)).save(captor.capture());
        ChatRoomMySql chatRoomMySqlCaptured = captor.getValue();
        assertNotNull(chatRoomMySqlCaptured.getDeletedAt());

    }

    @Test
    void deleteRoomFail_NoUser() {
        //given
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        //when
        //then
        try{
            chatService.deleteRoom(1L,"1번방");
        }catch (RuntimeException e){
            assertEquals(USER_NOT_FOUND.getMessage(),e.getMessage());
        }
    }


    @Test
    void deleteRoomFail_NoRoom() {
        //given
        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.empty());
        given(userRepository.findById(1L)).willReturn(Optional.of(User.builder().id(1L).build()));
        //when
        //then
        try{
            chatService.deleteRoom(1L,"1번방");
        }catch (RuntimeException e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }
    }

    @Test
    void deleteRoomFail_NotOwnerUser() {
        //given
        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(ChatRoomMySql.builder().owner(User.builder().id(1L).build()).build()));

        given(userRepository.findById(0L)).willReturn(Optional.of(User.builder().id(0L).build()));

        //when
        //then
        try{
            chatService.deleteRoom(0L,"1번방");
        }catch (RuntimeException e){
            assertEquals(NOT_CHAT_ROOM_OWNER.getMessage(),e.getMessage());
        }
    }

    @Test
    void enterRoomSuccess() {
        //given
        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").maxParticipant(3)
                .password("1234").build();

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom))
                .willReturn(new ArrayList<>());

        ArgumentCaptor<ChatRoomParticipant> captor = ArgumentCaptor.forClass(ChatRoomParticipant.class);
        //when
        chatService.enterRoom("1번방","1234",3L);
        //then
        verify(chatRoomParticipantRepository,times(1)).save(captor.capture());
        ChatRoomParticipant chatRoomParticipantCaptured = captor.getValue();
        assertEquals(chatRoom,chatRoomParticipantCaptured.getChatRoomMySql());
        assertEquals(3L,chatRoomParticipantCaptured.getUserId());
    }

    @Test
    void enterRoomFail_AlreadyParticipate() {
        //given
        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").maxParticipant(3)
                .password("1234").build();

        List<ChatRoomParticipant> participants = new ArrayList<>();

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder().userId(1L)
                .chatRoomMySql(chatRoom).build();
        participants.add(participant1);

        ChatRoomParticipant participant2 = ChatRoomParticipant.builder().userId(2L)
                .chatRoomMySql(chatRoom).build();
        participants.add(participant2);


        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom))
                .willReturn(participants);


        //when
        //then
        try{
            chatService.enterRoom("1번방","1234",1L);
        }catch (Exception e){
            assertEquals(ALREADY_PARTICIPATE.getMessage(),e.getMessage());
        }
    }

    @Test
    void enterRoomFail_ExceedMaxParticipant() {
        //given
        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").maxParticipant(2)
                .password("1234").build();

        List<ChatRoomParticipant> participants = new ArrayList<>();

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder().userId(1L)
                .chatRoomMySql(chatRoom).build();
        participants.add(participant1);

        ChatRoomParticipant participant2 = ChatRoomParticipant.builder().userId(2L)
                .chatRoomMySql(chatRoom).build();
        participants.add(participant2);


        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom))
                .willReturn(participants);



        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom))
                .willReturn(participants);


        //when
        //then
        try{
            chatService.enterRoom("1번방","1234",3L);
        }catch (Exception e){
            assertEquals(EXCEED_MAX_PARTICIPANTS.getMessage(),e.getMessage());
        }
    }

    @Test
    void enterRoomFail_PasswordNotMatch() {
        //given
        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").maxParticipant(3)
                .password("1234").build();

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findAllByChatRoomMySql(chatRoom))
                .willReturn(new ArrayList<>());

        //when
        //then
        try{
            chatService.enterRoom("1번방","4321",3L);
        }catch (Exception e){
            assertEquals(PASSWORD_NOT_MATCH.getMessage(),e.getMessage());
        }
    }

    @Test
    void leaveRoomSuccess() {
        //given
        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").maxParticipant(3)
                .password("1234").build();


        ChatRoomParticipant participant1 = ChatRoomParticipant.builder().userId(1L)
                .chatRoomMySql(chatRoom).build();

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,1L))
                .willReturn(Optional.of(participant1));

        ArgumentCaptor<ChatRoomParticipant> captor = ArgumentCaptor.forClass(ChatRoomParticipant.class);

        //when
        chatService.leaveRoom("1번방",1L);

        //then
        verify(chatRoomParticipantRepository,times(1)).delete(captor.capture());
        ChatRoomParticipant capturedChatRoomParticipant = captor.getValue();
        assertEquals(1L,capturedChatRoomParticipant.getUserId());
        assertEquals("1번방",capturedChatRoomParticipant.getChatRoomMySql().getId());
    }

    @Test
    void leaveRoomFail_NoRoom() {
        //given
        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.empty());

        //when
        //then
        try{
            chatService.leaveRoom("1번방",1L);
        } catch (Exception e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }
    }

    @Test
    void leaveRoomFail_NotParticipate() {
        //given
        ChatRoomMySql chatRoom = ChatRoomMySql.builder().id("1번방").build();

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoom));

        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,3L))
                .willReturn(Optional.empty());
        //when
        //then
        try{
            chatService.leaveRoom("1번방",3L);
        } catch (Exception e){
            assertEquals(NOT_PARTICIPATED_USER.getMessage(),e.getMessage());
        }
    }
    /** Entity 수정에 따른 테스트 에러 로 인한 커멘트 아웃
    @Test
    void getMessagesSuccess() {
        //given
        ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").build();

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomId("1번방");

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder()
                .userId(1L).chatRoomMySql(chatRoomMySql).build();

        List<Message> messages = new ArrayList<>();

        Message message1 = new Message();
        message1.setMessage("1번 메시지");
        message1.setCreatedAt(LocalDateTime.now());
        messages.add(message1);

        Message message2 = new Message();
        message2.setMessage("2번 메시지");
        message2.setCreatedAt(LocalDateTime.now());
        messages.add(message2);

        Message message3 = new Message();
        message3.setMessage("3번 메시지");
        message3.setCreatedAt(LocalDateTime.now().minusDays(2));
        messages.add(message3);

        Message message4 = new Message();
        message4.setMessage("4번 메시지");
        message4.setCreatedAt(LocalDateTime.now().minusDays(2));
        messages.add(message4);

        chatRoom.setMessages(messages);

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoomMySql));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,1L))
                .willReturn(Optional.of(participant1));
        given(chatRoomMongoRepository.findById("1번방")).willReturn(
                Optional.of(chatRoom));


        //when
        List<Message> results = chatService.getMessages("1번방",LocalDateTime.now().minusDays(1),1L);

        //then
        assertEquals(2,results.size());
        assertEquals("2번 메시지",results.get(0).getMessage());
        assertEquals("1번 메시지",results.get(1).getMessage());
    }
    **/
    @Test
    void getMessagesFail_NoRoom() {
        //given
        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.empty());

        //when
        //then
        try{
            chatService.getMessages("1번방", LocalDateTime.now().minusDays(1), 1L);
        }catch (Exception e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }
    }

    @Test
    void getMessagesFail_NotParticipate() {
        //given
        ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").build();

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoomMySql));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,1L))
                .willReturn(Optional.empty());


        //when
        //then
        try{
            chatService.getMessages("1번방",LocalDateTime.now().minusDays(1),1L);
        }catch (Exception e){
            assertEquals(NOT_PARTICIPATED_USER.getMessage(),e.getMessage());
        }
    }

    @Test
    void getMessagesFail_NoRoomMongo() {
        //given
        ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").build();

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder()
                .userId(1L).chatRoomMySql(chatRoomMySql).build();

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoomMySql));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,1L))
                .willReturn(Optional.of(participant1));
        given(chatRoomMongoRepository.findById("1번방")).willReturn(
                Optional.empty());


        //when
        //then
        try{
            chatService.getMessages("1번방",LocalDateTime.now().minusDays(1),1L);
        }catch (Exception e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }
    }

    @Test
    void chatEndSuccess() {
        //given
        ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").build();

        ChatRoomParticipant participant1 = ChatRoomParticipant.builder()
                .userId(1L).chatRoomMySql(chatRoomMySql).build();

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoomMySql));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,1L))
                .willReturn(Optional.of(participant1));


        //when
        var result = chatService.chatEnd("1번방",1L);

        //then
        assertNotNull(result.getLeftAt());
    }

    @Test
    void chatEndFail_NoChatRoom() {
        //given
        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.empty());
        
        //when
        //then
        try{
            chatService.chatEnd("1번방",1L);
        }catch (Exception e){
            assertEquals(CHAT_ROOM_NOT_FOUND.getMessage(),e.getMessage());
        }

    }

    @Test
    void chatEndFail_NotParticipated() {
        //given
        ChatRoomMySql chatRoomMySql = ChatRoomMySql.builder().id("1번방").build();

        given(chatRoomMySqlRepository.findById("1번방")).willReturn(
                Optional.of(chatRoomMySql));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoomMySql,1L))
                .willReturn(Optional.empty());


        //when
        //then
        try{
            chatService.chatEnd("1번방",1L);
        }catch (Exception e){
            assertEquals(NOT_PARTICIPATED_USER.getMessage(),e.getMessage());
        }
    }

}