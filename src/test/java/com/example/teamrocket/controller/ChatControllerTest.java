package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.error.exception.ChatRoomException;
import com.example.teamrocket.error.exception.UserException;
import com.example.teamrocket.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.teamrocket.error.type.ChatRoomErrorCode.TRAVEL_START_DATE_MUST_BE_BEFORE_END_DATE;
import static com.example.teamrocket.error.type.UserErrorCode.USER_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureDataMongo
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

        given(chatService.createRoom(anyLong(),any())).willReturn(
                ChatRoomDto.builder()
                        .build()
        );

        mockMvc.perform(post("/api/v1/chat/room")
                .header("X_AUTH_TOKEN","111")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new ChatRoomCreateInput()
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").isNotEmpty())
                .andDo(print());

    }

    @Test
    void createRoomFail_UserException() throws Exception{

        doThrow(new UserException(USER_NOT_FOUND))
                .when(chatService).createRoom(any(),any());

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
                .when(chatService).createRoom(any(),any());

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
}
