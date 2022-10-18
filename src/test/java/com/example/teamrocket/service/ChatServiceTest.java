package com.example.teamrocket.service;

import com.example.teamrocket.chatRoom.domain.ChatRoomInput;
import com.example.teamrocket.chatRoom.entity.mysql.ChatRoomMySql;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                .title("채팅방3").privateRoom(false).build();
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
    void editRoom() {
    }

    @Test
    void deleteRoom() {
    }

    @Test
    void enterRoom() {
    }

    @Test
    void leaveRoom() {
    }

    @Test
    void getMessages() {
    }
}