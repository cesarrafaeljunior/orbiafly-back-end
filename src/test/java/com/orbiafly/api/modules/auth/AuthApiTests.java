package com.orbiafly.api.modules.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "app.security.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void registersLogsInAndProtectsPrivateEndpoints() throws Exception {
    mockMvc.perform(get("/api/v1/accounts"))
        .andExpect(status().isUnauthorized());

    JsonNode registered = performJson(post("/api/v1/auth/register"), Map.of(
        "username", "ana",
        "email", "ana@example.com",
        "password", "senha123"), 201);

    mockMvc.perform(get("/api/v1/auth/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + registered.get("token").asText()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("ana@example.com"))
        .andExpect(jsonPath("$.role").value("ROLE_USER"));

    JsonNode logged = performJson(post("/api/v1/auth/login"), Map.of(
        "login", "ana@example.com",
        "password", "senha123"), 200);

    mockMvc.perform(get("/api/v1/accounts")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + logged.get("token").asText()))
        .andExpect(status().isOk());
  }

  private JsonNode performJson(
      org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
      Object payload,
      int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(request
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }
}
