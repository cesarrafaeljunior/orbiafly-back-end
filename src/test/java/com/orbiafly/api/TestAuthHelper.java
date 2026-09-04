package com.orbiafly.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class TestAuthHelper {

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private String adminToken;

  public TestAuthHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
  }

  public MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder request)
      throws Exception {
    return request.header("Authorization", "Bearer " + adminToken());
  }

  public String registerAndLogin(String email, String username, String password) throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of(
                "email", email,
                "username", username,
                "password", password))))
        .andExpect(MockMvcResultMatchers.status().isCreated());
    return login(username, password);
  }

  public String adminToken() throws Exception {
    if (adminToken == null) {
      adminToken = login("admin", "admin");
    }
    return adminToken;
  }

  public String login(String login, String password) throws Exception {
    String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of(
                "login", login,
                "password", password))))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    JsonNode json = objectMapper.readTree(response);
    return json.get("accessToken").asText();
  }
}
