package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.error.exception.ChatRoomException;
import com.example.teamrocket.error.exception.UserException;
import com.example.teamrocket.service.ChatService;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.example.teamrocket.error.type.ChatRoomErrorCode.TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE;
import static com.example.teamrocket.error.type.UserErrorCode.USER_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                        .header("X_AUTH_TOKEN","111")
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

        mockMvc.perform(get("/api/v1/chat/room-list?page=0&size=5&rcate1=서울시&rcate2=동작구")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChatRoomCreateInput()
                        )))
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


}
