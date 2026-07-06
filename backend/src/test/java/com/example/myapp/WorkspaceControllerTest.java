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
class WorkspaceControllerTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private String token;
    private int otherUserId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        token = registerAndLogin("wsowner", "wsowner@example.com");

        String registerResponse = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"wsmember","email":"wsmember@example.com","password":"Test@1234"}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        otherUserId = objectMapper.readTree(registerResponse).get("id").asInt();
    }

    @Test
    void createWorkspace_returns201WithName() throws Exception {
        mockMvc.perform(post("/api/workspaces")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"workspaceName":"My Workspace"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Workspace"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getWorkspaceById_returnsWorkspace() throws Exception {
        String createResponse = mockMvc.perform(post("/api/workspaces")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"workspaceName":"Fetch Workspace"}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int workspaceId = objectMapper.readTree(createResponse).get("id").asInt();

        mockMvc.perform(get("/api/workspaces/" + workspaceId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fetch Workspace"));
    }

    @Test
    void addMember_then_removeMember() throws Exception {
        String createResponse = mockMvc.perform(post("/api/workspaces")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"workspaceName":"Member Workspace"}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int workspaceId = objectMapper.readTree(createResponse).get("id").asInt();

        mockMvc.perform(post("/api/workspaces/" + workspaceId + "/members")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":" + otherUserId + ",\"role\":\"MEMBER\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/workspaces/" + workspaceId + "/members")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(delete("/api/workspaces/" + workspaceId + "/members/" + otherUserId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteWorkspace_returns204() throws Exception {
        String createResponse = mockMvc.perform(post("/api/workspaces")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"workspaceName":"Delete Me"}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int workspaceId = objectMapper.readTree(createResponse).get("id").asInt();

        mockMvc.perform(delete("/api/workspaces/" + workspaceId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
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
