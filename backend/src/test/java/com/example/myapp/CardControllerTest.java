package com.example.myapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CardControllerTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private String token;
    private int laneId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        token = registerAndLogin("cardowner", "cardowner@example.com");

        String wsResponse = mockMvc.perform(post("/api/workspaces")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"workspaceName":"Card Test WS"}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int workspaceId = objectMapper.readTree(wsResponse).get("id").asInt();

        String boardResponse = mockMvc.perform(post("/api/boards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workspaceId\":" + workspaceId + ",\"boardName\":\"Card Board\",\"visibility\":\"PUBLIC\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int boardId = objectMapper.readTree(boardResponse).get("boardId").asInt();

        String laneResponse = mockMvc.perform(post("/api/lanes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"boardId\":" + boardId + ",\"laneName\":\"To Do\",\"position\":0}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        laneId = objectMapper.readTree(laneResponse).get("id").asInt();
    }

    @Test
    void createCard_returns201() throws Exception {
        mockMvc.perform(post("/api/cards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"laneId\":" + laneId + ",\"title\":\"Fix bug\",\"position\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Fix bug"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getCardsByLane_returnsList() throws Exception {
        mockMvc.perform(post("/api/cards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"laneId\":" + laneId + ",\"title\":\"Card One\",\"position\":0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/cards/lane/" + laneId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Card One"));
    }

    @Test
    void archiveCard_then_unarchiveCard() throws Exception {
        String createResponse = mockMvc.perform(post("/api/cards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"laneId\":" + laneId + ",\"title\":\"Archive Me\",\"position\":0}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int cardId = objectMapper.readTree(createResponse).get("id").asInt();

        mockMvc.perform(patch("/api/cards/" + cardId + "/archive")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));

        mockMvc.perform(patch("/api/cards/" + cardId + "/unarchive")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(false));
    }

    @Test
    void moveCard_toNewPosition() throws Exception {
        String createResponse = mockMvc.perform(post("/api/cards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"laneId\":" + laneId + ",\"title\":\"Move Me\",\"position\":0}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int cardId = objectMapper.readTree(createResponse).get("id").asInt();

        mockMvc.perform(patch("/api/cards/" + cardId + "/move")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"laneId\":" + laneId + ",\"position\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId));
    }

    private String registerAndLogin(String username, String email) throws Exception {
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"email\":\"" + email + "\",\"password\":\"Test@1234\"}"))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"Test@1234\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }
}
