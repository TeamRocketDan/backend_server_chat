package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.domain.*;
import com.example.teamrocket.error.exception.ChatRoomException;
import com.example.teamrocket.error.exception.UserException;
import com.example.teamrocket.service.ChatService;
import com.example.teamrocket.utils.MessagePagingResponse;
import com.example.teamrocket.utils.PagingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.example.teamrocket.error.type.ChatRoomErrorCode.*;
import static com.example.teamrocket.error.type.UserErrorCode.USER_NOT_FOUND;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureDataMongo
@AutoConfigureMockMvc(addFilters = false)
public class ChatControllerTest {

    @MockBean
    private ChatService chatService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRoomSuccess() throws Exception{

        given(chatService.createRoom(any())).willReturn(
                ChatRoomDto.builder()
                        .id("1번방")
                        .title("1번방 제목")
                        .startDate(LocalDate.now().plusDays(2))
                        .endDate(LocalDate.now().plusDays(2))
                        .curParticipant(3)
                        .maxParticipant(6)
                        .privateRoom(false)
                        .ownerNickName("주인장")
                        .ownerProfileImage("사진사진")
                        .rcate1("서울시")
                        .rcate2("동작구")
                        .latitude("위도")
                        .longitude("경도")
                        .build()
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        mockMvc.perform(post("/api/v1/chat/room")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new ChatRoomCreateInput()
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.id").value("1번방"))
                .andExpect(jsonPath("$.result.title").value("1번방 제목"))
                .andExpect(jsonPath("$.result.startDate").value(formatter.format(LocalDate.now().plusDays(2))))
                .andExpect(jsonPath("$.result.endDate").value(formatter.format(LocalDate.now().plusDays(2))))
                .andExpect(jsonPath("$.result.curParticipant").value(3))
                .andExpect(jsonPath("$.result.maxParticipant").value(6))
                .andExpect(jsonPath("$.result.privateRoom").value(false))
                .andExpect(jsonPath("$.result.ownerNickName").value("주인장"))
                .andExpect(jsonPath("$.result.ownerProfileImage").value("사진사진"))
                .andExpect(jsonPath("$.result.rcate1").value("서울시"))
                .andExpect(jsonPath("$.result.rcate2").value("동작구"))
                .andExpect(jsonPath("$.result.longitude").value("경도"))
                .andExpect(jsonPath("$.result.latitude").value("위도"))
                .andDo(print());

    }

    @Test
    void createRoomFail_UserException() throws Exception{

        doThrow(new UserException(USER_NOT_FOUND))
                .when(chatService).createRoom(any());

        mockMvc.perform(post("/api/v1/chat/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRoomCreateInput()
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(USER_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("여행 시작날짜 끝날짜 순서 에러")
    void createRoomFail_TravelDateError() throws Exception{

        doThrow(new ChatRoomException(TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE))
                .when(chatService).createRoom(any());

        mockMvc.perform(post("/api/v1/chat/room")
                        .header("X_AUTH_TOKEN","111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRoomCreateInput()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE.getMessage()))
                .andDo(print());
    }

    @Test
    void listRoomSuccess() throws Exception{

        List<ChatRoomDto> chatRoomDtos = new ArrayList<>(5);
        chatRoomDtos.add(ChatRoomDto.builder().id("채팅방1").build());
        chatRoomDtos.add(ChatRoomDto.builder().id("채팅방2").build());

        PagingResponse result = PagingResponse.builder()
                .firstPage(false)
                .lastPage(true)
                .totalPage(20)
                .totalElements(100)
                .size(5)
                .currentPage(20)
                .build();

        result.setContent(chatRoomDtos);

        given(chatService.listRoom(anyString(),anyString(),any())).willReturn(
                result);

        mockMvc.perform(get("/api/v1/chat/room-list?page=0&size=5&rcate1=서울시&rcate2=동작구"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.firstPage").value(false))
                .andExpect(jsonPath("$.result.lastPage").value(true))
                .andExpect(jsonPath("$.result.totalPage").value(20))
                .andExpect(jsonPath("$.result.totalElements").value(100))
                .andExpect(jsonPath("$.result.size").value(5))
                .andExpect(jsonPath("$.result.currentPage").value(20))
                .andExpect(jsonPath("$.result.content[0]").isNotEmpty())
                .andExpect(jsonPath("$.result.content[2]").doesNotExist())
                .andDo(print());
    }

    @Test
    void myListRoomSuccess() throws Exception{

        List<ChatRoomDto> chatRoomDtos = new ArrayList<>(5);
        chatRoomDtos.add(ChatRoomDto.builder().id("채팅방1").build());
        chatRoomDtos.add(ChatRoomDto.builder().id("채팅방2").build());

        PagingResponse result = PagingResponse.builder()
                .firstPage(false)
                .lastPage(true)
                .totalPage(20)
                .totalElements(100)
                .size(5)
                .currentPage(20)
                .build();

        result.setContent(chatRoomDtos);

        given(chatService.myListRoom(any())).willReturn(
                result);

        mockMvc.perform(get("/api/v1/chat/my-room-list?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.firstPage").value(false))
                .andExpect(jsonPath("$.result.lastPage").value(true))
                .andExpect(jsonPath("$.result.totalPage").value(20))
                .andExpect(jsonPath("$.result.totalElements").value(100))
                .andExpect(jsonPath("$.result.size").value(5))
                .andExpect(jsonPath("$.result.currentPage").value(20))
                .andExpect(jsonPath("$.result.content[0]").isNotEmpty())
                .andExpect(jsonPath("$.result.content[2]").doesNotExist())
                .andDo(print());
    }

    @Test
    void myListRoomFail_UserException() throws Exception{

        doThrow(new UserException(USER_NOT_FOUND))
                .when(chatService).myListRoom(any());

        mockMvc.perform(get("/api/v1/chat/my-room-list?page=0&size=5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(USER_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void editRoomSuccess() throws Exception{

        given(chatService.editRoom(eq("1번방"),any())).willReturn(
                ChatRoomDto.builder()
                        .id("1번방")
                        .title("1번방 제목2")
                        .startDate(LocalDate.now().plusDays(2))
                        .endDate(LocalDate.now().plusDays(2))
                        .curParticipant(3)
                        .maxParticipant(6)
                        .privateRoom(false)
                        .ownerNickName("주인장")
                        .ownerProfileImage("사진사진")
                        .rcate1("서울시")
                        .rcate2("동작구")
                        .latitude("위도")
                        .longitude("경도")
                        .build()
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        mockMvc.perform(patch("/api/v1/chat/room/1번방")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRoomEditInput()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.id").value("1번방"))
                .andExpect(jsonPath("$.result.title").value("1번방 제목2"))
                .andExpect(jsonPath("$.result.startDate").value(formatter.format(LocalDate.now().plusDays(2))))
                .andExpect(jsonPath("$.result.endDate").value(formatter.format(LocalDate.now().plusDays(2))))
                .andExpect(jsonPath("$.result.curParticipant").value(3))
                .andExpect(jsonPath("$.result.maxParticipant").value(6))
                .andExpect(jsonPath("$.result.privateRoom").value(false))
                .andExpect(jsonPath("$.result.ownerNickName").value("주인장"))
                .andExpect(jsonPath("$.result.ownerProfileImage").value("사진사진"))
                .andExpect(jsonPath("$.result.rcate1").value("서울시"))
                .andExpect(jsonPath("$.result.rcate2").value("동작구"))
                .andExpect(jsonPath("$.result.longitude").value("경도"))
                .andExpect(jsonPath("$.result.latitude").value("위도"))
                .andDo(print());
    }

    @Test
    void editRoomFail_NoUser() throws Exception{

        doThrow(new UserException(USER_NOT_FOUND))
                .when(chatService).editRoom(eq("1번방"),any());

        mockMvc.perform(patch("/api/v1/chat/room/1번방")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRoomEditInput()
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(USER_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void editRoomFail_NoChatRoom() throws Exception{

        doThrow(new ChatRoomException(CHAT_ROOM_NOT_FOUND))
                .when(chatService).editRoom(eq("1번방"),any());

        mockMvc.perform(patch("/api/v1/chat/room/1번방")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRoomEditInput()
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(CHAT_ROOM_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void editRoomFail_NotOwnerUser() throws Exception{

        doThrow(new ChatRoomException(NOT_CHAT_ROOM_OWNER))
                .when(chatService).editRoom(eq("1번방"),any());

        mockMvc.perform(patch("/api/v1/chat/room/1번방")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRoomEditInput()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(NOT_CHAT_ROOM_OWNER.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("여행 시작날짜 에러 - 오늘 이후로 수정할 수 있습니다.")
    void editRoomFail_StartDateUntilTodayNotPermitted() throws Exception{

        doThrow(new ChatRoomException(START_DATE_MUST_BE_AFTER_TODAY))
                .when(chatService).editRoom(eq("1번방"),any());

        mockMvc.perform(patch("/api/v1/chat/room/1번방")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRoomEditInput()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(START_DATE_MUST_BE_AFTER_TODAY.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("여행 시작날짜 이후로 여행 끝 날짜를 설정할 수 있습니다.")
    void editRoomFail_TravelDateError() throws Exception{

        doThrow(new ChatRoomException(TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE))
                .when(chatService).editRoom(eq("1번방"),any());

        mockMvc.perform(patch("/api/v1/chat/room/1번방")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRoomEditInput()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("최대 참가자수는 현재 참가자 수 이상으로 설정해야합니다.")
    void editRoomFail_MaxParticipantError() throws Exception{

        doThrow(new ChatRoomException(MAX_PARTICIPANT_IS_TOO_SMALL))
                .when(chatService).editRoom(eq("1번방"),any());

        mockMvc.perform(patch("/api/v1/chat/room/1번방")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRoomEditInput()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(MAX_PARTICIPANT_IS_TOO_SMALL.getMessage()))
                .andDo(print());
    }

    @Test
    void deleteRoomSuccess() throws Exception{
        doNothing().when(chatService).deleteRoom(eq("1번방"));

        mockMvc.perform(delete("/api/v1/chat/room/1번방"))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    void deleteRoomFail_NoUser() throws Exception{
        doThrow(new UserException(USER_NOT_FOUND))
                .when(chatService).deleteRoom(eq("1번방"));

        mockMvc.perform(delete("/api/v1/chat/room/1번방"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(USER_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void deleteRoomSuccess_NoChatRoom() throws Exception{
        doThrow(new ChatRoomException(CHAT_ROOM_NOT_FOUND))
                .when(chatService).deleteRoom(eq("1번방"));

        mockMvc.perform(delete("/api/v1/chat/room/1번방"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(CHAT_ROOM_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void deleteRoomSuccess_NotOwnerUser() throws Exception{
        doThrow(new ChatRoomException(NOT_CHAT_ROOM_OWNER))
                .when(chatService).deleteRoom(eq("1번방"));

        mockMvc.perform(delete("/api/v1/chat/room/1번방"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(NOT_CHAT_ROOM_OWNER.getMessage()))
                .andDo(print());
    }

    @Test
    void enterRoomSuccess() throws Exception{

        given(chatService.enterRoom(eq("1번방"),eq("1234"))).willReturn(
                new ChatRoomServiceResult("1번방",1L)
        );

        mockMvc.perform(patch("/api/v1/chat/room-enter/1번방?password=1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.chatRoomId").value("1번방"))
                .andExpect(jsonPath("$.result.userId").value(1L))
                .andDo(print());
    }

    @Test
    void enterRoomFail_NoUser() throws Exception{
        doThrow(new UserException(USER_NOT_FOUND))
                .when(chatService).enterRoom(eq("1번방"),any());

        mockMvc.perform(patch("/api/v1/chat/room-enter/1번방?password=1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(USER_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void enterRoomFail_NoChatRoom() throws Exception{
        doThrow(new ChatRoomException(CHAT_ROOM_NOT_FOUND))
                .when(chatService).enterRoom(eq("1번방"),any());

        mockMvc.perform(patch("/api/v1/chat/room-enter/1번방?password=1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(CHAT_ROOM_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void enterRoomFail_PasswordNotMatch() throws Exception{
        doThrow(new ChatRoomException(PASSWORD_NOT_MATCH))
                .when(chatService).enterRoom(eq("1번방"),any());

        mockMvc.perform(patch("/api/v1/chat/room-enter/1번방?password=1234"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(PASSWORD_NOT_MATCH.getMessage()))
                .andDo(print());
    }

    @Test
    void enterRoomFail_ExceedMaxParticipants() throws Exception{
        doThrow(new ChatRoomException(EXCEED_MAX_PARTICIPANTS))
                .when(chatService).enterRoom(eq("1번방"),any());

        mockMvc.perform(patch("/api/v1/chat/room-enter/1번방?password="))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(EXCEED_MAX_PARTICIPANTS.getMessage()))
                .andDo(print());
    }

    @Test
    void leaveRoomSuccess() throws Exception{

        given(chatService.leaveRoom(eq("1번방"))).willReturn(
                new ChatRoomServiceResult("1번방",1L)
        );

        mockMvc.perform(patch("/api/v1/chat/room-leave/1번방"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.chatRoomId").value("1번방"))
                .andExpect(jsonPath("$.result.userId").value(1L))
                .andDo(print());
    }

    @Test
    void leaveRoomFail_NoUser() throws Exception{

        doThrow(new UserException(USER_NOT_FOUND))
                .when(chatService).leaveRoom(eq("1번방"));

        mockMvc.perform(patch("/api/v1/chat/room-leave/1번방"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(USER_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void leaveRoomFail_NoChatRoom() throws Exception{

        doThrow(new ChatRoomException(CHAT_ROOM_NOT_FOUND))
                .when(chatService).leaveRoom(eq("1번방"));

        mockMvc.perform(patch("/api/v1/chat/room-leave/1번방"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(CHAT_ROOM_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void leaveRoomFail_NotParticipatedUser() throws Exception{

        doThrow(new ChatRoomException(NOT_PARTICIPATED_USER))
                .when(chatService).leaveRoom(eq("1번방"));

        mockMvc.perform(patch("/api/v1/chat/room-leave/1번방"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(NOT_PARTICIPATED_USER.getMessage()))
                .andDo(print());
    }

    @Test
    void getMessagesSuccess() throws Exception{

        MessagePagingResponse response = MessagePagingResponse.builder()
                .lastDay(false)
                .targetDay(LocalDate.now())
                .firstPage(true)
                .lastPage(false)
                .targetDayTotalPage(20)
                .targetDayTotalElements(100)
                .size(5)
                .targetDayCurrentPage(0)
                .content(new ArrayList<>())
                .build();

        given(chatService.getMessages(eq("1번방"),eq(LocalDate.now()),eq(0),eq(5))).willReturn(
                response);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        mockMvc.perform(get("/api/v1/chat/message/1번방?date="+formatter.format(LocalDate.now())+"&page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.lastDay").value(false))
                .andExpect(jsonPath("$.result.targetDay").isNotEmpty())
                .andExpect(jsonPath("$.result.firstPage").value(true))
                .andExpect(jsonPath("$.result.lastPage").value(false))
                .andExpect(jsonPath("$.result.targetDayTotalPage").value(20))
                .andExpect(jsonPath("$.result.targetDayTotalElements").value(100))
                .andExpect(jsonPath("$.result.size").value(5))
                .andExpect(jsonPath("$.result.targetDayCurrentPage").value(0))
                .andDo(print());
    }

    @Test
    void getMessagesFail_NoUser() throws Exception{

        doThrow(new UserException(USER_NOT_FOUND))
                .when(chatService).getMessages(eq("1번방"),eq(LocalDate.now()),eq(0),eq(5));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        mockMvc.perform(get("/api/v1/chat/message/1번방?date="+formatter.format(LocalDate.now())+"&page=0&size=5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(USER_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void getMessagesFail_NoChatRoom() throws Exception{

        doThrow(new ChatRoomException(CHAT_ROOM_NOT_FOUND))
                .when(chatService).getMessages(eq("1번방"),eq(LocalDate.now()),eq(0),eq(5));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        mockMvc.perform(get("/api/v1/chat/message/1번방?date="+formatter.format(LocalDate.now())+"&page=0&size=5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(CHAT_ROOM_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void getMessagesFail_NotParticipatedUser() throws Exception{

        doThrow(new ChatRoomException(NOT_PARTICIPATED_USER))
                .when(chatService).getMessages(eq("1번방"),eq(LocalDate.now()),eq(0),eq(5));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        mockMvc.perform(get("/api/v1/chat/message/1번방?date="+formatter.format(LocalDate.now())+"&page=0&size=5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(NOT_PARTICIPATED_USER.getMessage()))
                .andDo(print());
    }

        @Test
    void getMessagesMongoSuccess() throws Exception{

        MessagePagingResponse response = MessagePagingResponse.builder()
                .lastDay(false)
                .targetDay(LocalDate.now())
                .firstPage(true)
                .lastPage(false)
                .targetDayTotalPage(20)
                .targetDayTotalElements(100)
                .size(5)
                .targetDayCurrentPage(0)
                .content(new ArrayList<>())
                .build();
        given(chatService.getMessagesMongo(eq("1번방"),eq(0),eq(5))).willReturn(
            response);

        mockMvc.perform(get("/api/v1/chat/message/mongo/1번방?&page=0&size=5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.lastDay").value(false))
            .andExpect(jsonPath("$.result.targetDay").isNotEmpty())
            .andExpect(jsonPath("$.result.firstPage").value(true))
            .andExpect(jsonPath("$.result.lastPage").value(false))
            .andExpect(jsonPath("$.result.targetDayTotalPage").value(20))
            .andExpect(jsonPath("$.result.targetDayTotalElements").value(100))
            .andExpect(jsonPath("$.result.size").value(5))
            .andExpect(jsonPath("$.result.targetDayCurrentPage").value(0))
            .andDo(print());
    }

    @Test
    void getMessagesMongoFail_NoUser() throws Exception{

        doThrow(new UserException(USER_NOT_FOUND))
                .when(chatService).getMessagesMongo(eq("1번방"),eq(0),eq(5));

        mockMvc.perform(get("/api/v1/chat/message/mongo/1번방?&page=0&size=5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(USER_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void getMessagesMongoFail_NoChatRoom() throws Exception{

        doThrow(new ChatRoomException(CHAT_ROOM_NOT_FOUND))
                .when(chatService).getMessagesMongo(eq("1번방"),eq(0),eq(5));

        mockMvc.perform(get("/api/v1/chat/message/mongo/1번방?&page=0&size=5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(CHAT_ROOM_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void getMessagesMongoFail_NotParticipatedUser() throws Exception{

        doThrow(new ChatRoomException(NOT_PARTICIPATED_USER))
                .when(chatService).getMessagesMongo(eq("1번방"),eq(0),eq(5));

        mockMvc.perform(get("/api/v1/chat/message/mongo/1번방?&page=0&size=5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(NOT_PARTICIPATED_USER.getMessage()))
                .andDo(print());
    }


    @Test
    void chatEndSuccess() throws Exception{

        given(chatService.chatEnd(eq("1번방"))).willReturn(
                ChatRoomParticipantDto.builder()
                        .userId(1L)
                        .leftAt(LocalDateTime.now())
                        .build());

        mockMvc.perform(patch("/api/v1/chat/chat-end/1번방"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.userId").value(1))
                .andExpect(jsonPath("$.result.leftAt").isNotEmpty())
                .andDo(print());
    }

    @Test
    void chatEndFail_NoUser() throws Exception{

        doThrow(new UserException(USER_NOT_FOUND))
                .when(chatService).chatEnd(eq("1번방"));

        mockMvc.perform(patch("/api/v1/chat/chat-end/1번방"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(USER_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void chatEndFail_NoChatRoom() throws Exception{

        doThrow(new ChatRoomException(CHAT_ROOM_NOT_FOUND))
                .when(chatService).chatEnd(eq("1번방"));

        mockMvc.perform(patch("/api/v1/chat/chat-end/1번방"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(CHAT_ROOM_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    void chatEndFail_NotParticipatedUser() throws Exception{

        doThrow(new ChatRoomException(NOT_PARTICIPATED_USER))
                .when(chatService).chatEnd(eq("1번방"));

        mockMvc.perform(patch("/api/v1/chat/chat-end/1번방"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.result").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(NOT_PARTICIPATED_USER.getMessage()))
                .andDo(print());
    }

}
