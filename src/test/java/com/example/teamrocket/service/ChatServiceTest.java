package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomInput;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomParticipant;
import com.example.teamrocket.chatRoom.repository.ChatRoomMongoRepository;
import com.example.teamrocket.chatRoom.repository.ChatRoomMySqlRepository;
import com.example.teamrocket.chatRoom.repository.ChatRoomParticipantRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

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
        ChatRoomInput input = ChatRoomInput.builder()
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

        ArgumentCaptor<ChatRoomMySql> captor = ArgumentCaptor.forClass(ChatRoomMySql.class);
        //when
        chatService.createRoom(1L,input);

        //then
        verify(chatRoomMySqlRepository,times(1)).save(captor.capture());
        ChatRoomMySql chatRoomMySqlCaptured = captor.getValue();
        assertEquals(1L,chatRoomMySqlCaptured.getUserId());
        assertEquals("채팅방1",chatRoomMySqlCaptured.getTitle());
        assertEquals(8,chatRoomMySqlCaptured.getMaxParticipant());
        assertEquals("1234",chatRoomMySqlCaptured.getPassword());
        assertEquals("rcate1",chatRoomMySqlCaptured.getRcate1());
        assertEquals("rcate2",chatRoomMySqlCaptured.getRcate2());
        assertEquals("rcate3",chatRoomMySqlCaptured.getRcate3());
        assertEquals("위도",chatRoomMySqlCaptured.getLongitude());
        assertEquals("경도",chatRoomMySqlCaptured.getLatitude());

    }

    @Test
    void listRoomSuccess() {
        //given
        List<ChatRoomMySql> roomLists = new ArrayList<>();
        ChatRoomMySql room1 = ChatRoomMySql.builder()
                .title("채팅방1").privateRoom(true).build();
        ChatRoomMySql room2 = ChatRoomMySql.builder()
                .title("채팅방2").privateRoom(false).build();
        ChatRoomMySql room3 = ChatRoomMySql.builder()
                .title("채팅방3").privateRoom(true).deletedAt(LocalDateTime.now()).build();
        ChatRoomMySql room4 = ChatRoomMySql.builder()
                .title("채팅방4").privateRoom(true).build();

        roomLists.add(room1);
        roomLists.add(room2);
        roomLists.add(room3);
        roomLists.add(room4);

        given(chatRoomMySqlRepository.findAll()).willReturn(roomLists);
        //when
        var results = chatService.listRoom();
        //then
        assertEquals(2,results.size());
        assertEquals("채팅방2",results.get(0).getTitle());
        assertEquals("채팅방3",results.get(1).getTitle());

    }

    @Test
    void editRoomSuccess() {

        //given
        ChatRoomInput input = ChatRoomInput.builder()
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

        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.of(ChatRoomMySql.builder().userId(1L).build()));

        given(chatRoomMySqlRepository.save(any())).willReturn(new ChatRoomMySql());

        ArgumentCaptor<ChatRoomMySql> captor = ArgumentCaptor.forClass(ChatRoomMySql.class);
        //when
        chatService.editRoom(1L,1L,input);

        //then
        verify(chatRoomMySqlRepository,times(1)).save(captor.capture());
        ChatRoomMySql chatRoomMySqlCaptured = captor.getValue();
        assertEquals("채팅방1",chatRoomMySqlCaptured.getTitle());
        assertEquals(8,chatRoomMySqlCaptured.getMaxParticipant());
        assertEquals("1234",chatRoomMySqlCaptured.getPassword());
        assertEquals("rcate1",chatRoomMySqlCaptured.getRcate1());
        assertEquals("rcate2",chatRoomMySqlCaptured.getRcate2());
        assertEquals("rcate3",chatRoomMySqlCaptured.getRcate3());
        assertEquals("위도",chatRoomMySqlCaptured.getLongitude());
        assertEquals("경도",chatRoomMySqlCaptured.getLatitude());
    }

    @Test
    void editRoomFail_NoRoom() {

        //given
        ChatRoomInput input = new ChatRoomInput();

        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.empty());

        //when
        //then
        try{
            chatService.editRoom(1L,1L,input);
        }catch (RuntimeException e){
            assertEquals("방을 찾을 수 없습니다.",e.getMessage());
        }

    }

    @Test
    void editRoomFail_NotOwnerUser() {

        //given
        ChatRoomInput input = new ChatRoomInput();

        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.of(ChatRoomMySql.builder().userId(1L).build()));

        //when
        //then
        try{
            chatService.editRoom(0L,1L,input);
        }catch (RuntimeException e){
            assertEquals("방장이 아닙니다.",e.getMessage());
        }
    }


    @Test
    void deleteRoomSuccess() {
        //given
        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.of(ChatRoomMySql.builder().userId(1L).build()));

        ArgumentCaptor<ChatRoomMySql> captor = ArgumentCaptor.forClass(ChatRoomMySql.class);
        //when
        chatService.deleteRoom(1L,1L);

        //then
        verify(chatRoomMySqlRepository,times(1)).save(captor.capture());
        ChatRoomMySql chatRoomMySqlCaptured = captor.getValue();
        assertNotNull(chatRoomMySqlCaptured.getDeletedAt());

    }

    @Test
    void deleteRoomFail_NoRoom() {
        //given
        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.empty());

        //when
        //then
        try{
            chatService.deleteRoom(1L,1L);
        }catch (RuntimeException e){
            assertEquals("방을 찾을 수 없습니다.",e.getMessage());
        }
    }

    @Test
    void deleteRoomFail_NotOwnerUser() {
        //given
        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.of(ChatRoomMySql.builder().userId(1L).build()));

        //when
        //then
        try{
            chatService.deleteRoom(0L,1L);
        }catch (RuntimeException e){
            assertEquals("방장이 아닙니다.",e.getMessage());
        }
    }

    @Test
    void enterRoomSuccess() {
        //given
        ChatRoomMySql chatRoom = new ChatRoomMySql();
        chatRoom.setId(1L);
        chatRoom.setMaxParticipant(3);

        List<ChatRoomParticipant> participants = new ArrayList<>();

        ChatRoomParticipant participant1 = new ChatRoomParticipant();
        participant1.setUserId(1L);
        participant1.setChatRoomMySql(chatRoom);
        participants.add(participant1);

        ChatRoomParticipant participant2 = new ChatRoomParticipant();
        participant2.setUserId(2L);
        participant2.setChatRoomMySql(chatRoom);
        participants.add(participant2);

        chatRoom.setParticipants(participants);

        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,3L))
                .willReturn(Optional.empty());

        ArgumentCaptor<ChatRoomParticipant> captor = ArgumentCaptor.forClass(ChatRoomParticipant.class);
        //when
        chatService.enterRoom(1L,3L);
        //then
        verify(chatRoomParticipantRepository,times(1)).save(captor.capture());
        ChatRoomParticipant chatRoomParticipantCaptured = captor.getValue();
        assertEquals(chatRoom,chatRoomParticipantCaptured.getChatRoomMySql());
        assertEquals(3L,chatRoomParticipantCaptured.getUserId());
    }

    @Test
    void enterRoomFail_AlreadyParticipate() {
        //given
        ChatRoomMySql chatRoom = new ChatRoomMySql();
        chatRoom.setId(1L);
        chatRoom.setMaxParticipant(3);

        List<ChatRoomParticipant> participants = new ArrayList<>();

        ChatRoomParticipant participant1 = new ChatRoomParticipant();
        participant1.setUserId(1L);
        participant1.setChatRoomMySql(chatRoom);
        participants.add(participant1);

        ChatRoomParticipant participant2 = new ChatRoomParticipant();
        participant2.setUserId(2L);
        participant2.setChatRoomMySql(chatRoom);
        participants.add(participant2);

        chatRoom.setParticipants(participants);

        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,1L))
                .willReturn(Optional.of(participant1));


        //when
        //then
        try{
            chatService.enterRoom(1L,1L);
        }catch (Exception e){
            assertEquals("이미 방에 참가한 사람입니다.",e.getMessage());
        }
    }


    @Test
    void enterRoomFail_ExceedMaxParticipant() {
        //given
        ChatRoomMySql chatRoom = new ChatRoomMySql();
        chatRoom.setId(1L);
        chatRoom.setMaxParticipant(2);

        List<ChatRoomParticipant> participants = new ArrayList<>();

        ChatRoomParticipant participant1 = new ChatRoomParticipant();
        participant1.setUserId(1L);
        participant1.setChatRoomMySql(chatRoom);
        participants.add(participant1);

        ChatRoomParticipant participant2 = new ChatRoomParticipant();
        participant2.setUserId(2L);
        participant2.setChatRoomMySql(chatRoom);
        participants.add(participant2);

        chatRoom.setParticipants(participants);

        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,3L))
                .willReturn(Optional.empty());


        //when
        //then
        try{
            chatService.enterRoom(1L,3L);
        }catch (Exception e){
            assertEquals("정원을 넘어 들어갈 수 없습니다.",e.getMessage());
        }
    }

    @Test
    void leaveRoomSuccess() {
        //given
        ChatRoomMySql chatRoom = new ChatRoomMySql();
        chatRoom.setId(1L);

        List<ChatRoomParticipant> participants = new ArrayList<>();

        ChatRoomParticipant participant1 = new ChatRoomParticipant();
        participant1.setUserId(1L);
        participant1.setChatRoomMySql(chatRoom);
        participants.add(participant1);

        ChatRoomParticipant participant2 = new ChatRoomParticipant();
        participant2.setUserId(2L);
        participant2.setChatRoomMySql(chatRoom);
        participants.add(participant2);

        chatRoom.setParticipants(participants);

        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.of(chatRoom));
        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,1L))
                .willReturn(Optional.of(participant1));

        ArgumentCaptor<ChatRoomParticipant> captor = ArgumentCaptor.forClass(ChatRoomParticipant.class);

        //when
        chatService.leaveRoom(1L,1L);

        //then
        verify(chatRoomParticipantRepository,times(1)).delete(captor.capture());
        ChatRoomParticipant capturedChatRoomParticipant = captor.getValue();
        assertEquals(1L,capturedChatRoomParticipant.getUserId());
        assertEquals(1L,capturedChatRoomParticipant.getChatRoomMySql().getId());
    }

    @Test
    void leaveRoomFail_NoRoom() {
        //given
        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.empty());

        //when
        //then
        try{
            chatService.leaveRoom(1L,1L);
        } catch (Exception e){
            assertEquals("방을 찾을 수 없습니다.",e.getMessage());
        }
    }

    @Test
    void leaveRoomFail_NotParticipate() {
        //given
        ChatRoomMySql chatRoom = new ChatRoomMySql();
        chatRoom.setId(1L);

        given(chatRoomMySqlRepository.findById(1L)).willReturn(
                Optional.of(chatRoom));

        given(chatRoomParticipantRepository.findByChatRoomMySqlAndUserId(chatRoom,3L))
                .willReturn(Optional.empty());
        //when
        //then
        try{
            chatService.leaveRoom(1L,3L);
        } catch (Exception e){
            assertEquals("방에 참가한 이력이 없습니다.",e.getMessage());
        }
    }

    @Test
    void getMessages() {
    }
}