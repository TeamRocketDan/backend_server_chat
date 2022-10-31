package com.example.teamrocket.controller;

import com.example.teamrocket.chatRoom.domain.ChatRoomCreateInput;
import com.example.teamrocket.chatRoom.domain.ChatRoomDto;
import com.example.teamrocket.error.exception.ChatRoomException;
import com.example.teamrocket.error.exception.UserException;
import com.example.teamrocket.service.ChatService;
import com.example.teamrocket.service.ChatServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    void successCreateRoom() throws Exception{

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

}
