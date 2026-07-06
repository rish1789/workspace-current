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
class BoardControllerTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private String token;
    private int workspaceId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        token = registerAndLogin("boardowner", "boardowner@example.com");

        String wsResponse = mockMvc.perform(post("/api/workspaces")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"workspaceName":"Board Test WS"}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        workspaceId = objectMapper.readTree(wsResponse).get("id").asInt();
    }

    @Test
    void createBoard_returns201() throws Exception {
        mockMvc.perform(post("/api/boards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workspaceId\":" + workspaceId + ",\"boardName\":\"Test Board\",\"visibility\":\"PUBLIC\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.boardName").value("Test Board"))
                .andExpect(jsonPath("$.boardId").isNumber());
    }

    @Test
    void updateBoardVisibility_returnsUpdated() throws Exception {
        String createResponse = mockMvc.perform(post("/api/boards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workspaceId\":" + workspaceId + ",\"boardName\":\"Vis Board\",\"visibility\":\"PUBLIC\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int boardId = objectMapper.readTree(createResponse).get("boardId").asInt();

        mockMvc.perform(patch("/api/boards/" + boardId + "/visibility")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"visibility":"PRIVATE"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));
    }

    @Test
    void deleteBoard_returns204() throws Exception {
        String createResponse = mockMvc.perform(post("/api/boards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workspaceId\":" + workspaceId + ",\"boardName\":\"Delete Board\",\"visibility\":\"PUBLIC\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int boardId = objectMapper.readTree(createResponse).get("boardId").asInt();

        mockMvc.perform(delete("/api/boards/" + boardId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void getBoardsByWorkspace_returnsList() throws Exception {
        mockMvc.perform(post("/api/boards")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workspaceId\":" + workspaceId + ",\"boardName\":\"Board A\",\"visibility\":\"PUBLIC\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/boards/workspace/" + workspaceId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].boardName").value("Board A"));
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
