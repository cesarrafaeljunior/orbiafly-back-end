package com.orbiafly.api.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbiafly.api.TestAuthHelper;
import com.orbiafly.api.modules.auth.domain.AppRole;
import com.orbiafly.api.modules.auth.domain.AppUser;
import com.orbiafly.api.modules.auth.infrastructure.persistence.AppUserRepositoryInterface;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private AppUserRepositoryInterface userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private TestAuthHelper auth;

  @BeforeEach
  void cleanData() {
    auth = new TestAuthHelper(mockMvc, objectMapper);
    jdbcTemplate.update("DELETE FROM loan_entry");
    jdbcTemplate.update("DELETE FROM loan_installment");
    jdbcTemplate.update("DELETE FROM loan");
    jdbcTemplate.update("DELETE FROM credit_card_installment");
    jdbcTemplate.update("DELETE FROM credit_card_purchase");
    jdbcTemplate.update("DELETE FROM credit_card");
    jdbcTemplate.update("DELETE FROM installment_expense_installment");
    jdbcTemplate.update("DELETE FROM installment_purchase");
    jdbcTemplate.update("DELETE FROM fixed_expense_occurrence");
    jdbcTemplate.update("DELETE FROM fixed_expense");
    jdbcTemplate.update("DELETE FROM financial_transaction");
    jdbcTemplate.update("DELETE FROM financial_account");
    jdbcTemplate.update("DELETE FROM app_user_role WHERE user_id <> ?::uuid",
        "00000000-0000-0000-0000-000000000100");
    jdbcTemplate.update("DELETE FROM app_user WHERE id <> ?::uuid",
        "00000000-0000-0000-0000-000000000100");
  }

  @Test
  void registersUserWithDefaultRoleAndBcryptHash() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "email", "ana@example.com",
                "username", "ana",
                "password", "senha-segura"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accessToken", notNullValue()))
        .andExpect(jsonPath("$.user.email").value("ana@example.com"))
        .andExpect(jsonPath("$.user.username").value("ana"))
        .andExpect(jsonPath("$.user.roles", hasItem("ROLE_USER")))
        .andExpect(jsonPath("$.user.roles", not(hasItem("ROLE_ADMIN"))))
        .andReturn();

    JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
    UUID userId = UUID.fromString(response.at("/user/id").asText());
    AppUser stored = userRepository.findById(userId).orElseThrow();

    assertThat(stored.getPasswordHash()).isNotEqualTo("senha-segura");
    assertThat(passwordEncoder.matches("senha-segura", stored.getPasswordHash())).isTrue();
  }

  @Test
  void logsInWithEmailOrUsernameAndRejectsInvalidCredentials() throws Exception {
    auth.registerAndLogin("bruno@example.com", "bruno", "senha-segura");

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("login", "bruno@example.com", "password", "senha-segura"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken", containsString(".")));

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("login", "bruno", "password", "errada"))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void protectsFinancialEndpointsAndAcceptsBearerToken() throws Exception {
    mockMvc.perform(get("/api/v1/accounts"))
        .andExpect(status().isUnauthorized());

    String token = auth.registerAndLogin("carol@example.com", "carol", "senha-segura");
    mockMvc.perform(get("/api/v1/accounts")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accounts").isArray());
  }

  @Test
  void seedsAdminUserWithAdminRole() throws Exception {
    AppUser admin = userRepository.findByUsernameIgnoreCase("admin").orElseThrow();

    assertThat(admin.getEmail()).isEqualTo("admin@admin.com.br");
    assertThat(admin.getRoles()).contains(AppRole.ROLE_ADMIN, AppRole.ROLE_USER);

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("login", "admin", "password", "admin"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.roles", hasItem("ROLE_ADMIN")));
  }

  @Test
  void isolatesFinancialAccountsBetweenUsers() throws Exception {
    String firstToken = auth.registerAndLogin("dani@example.com", "dani", "senha-segura");
    String secondToken = auth.registerAndLogin("eva@example.com", "eva", "senha-segura");

    JsonNode created = performJson(
        post("/api/v1/accounts").header("Authorization", "Bearer " + firstToken),
        Map.of(
            "name", "Conta Dani",
            "type", "CHECKING",
            "institution", "Banco",
            "openingBalance", 300.00),
        201);
    String accountId = created.get("id").asText();

    mockMvc.perform(get("/api/v1/accounts/{id}", accountId)
            .header("Authorization", "Bearer " + secondToken))
        .andExpect(status().isNotFound());

    mockMvc.perform(get("/api/v1/accounts")
            .header("Authorization", "Bearer " + secondToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accounts").isEmpty())
        .andExpect(jsonPath("$.totalAvailableBalance").value(0));

    mockMvc.perform(get("/api/v1/accounts/{id}", accountId)
            .header("Authorization", "Bearer " + firstToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Conta Dani"));
  }

  private JsonNode performJson(
      org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
      Object body,
      int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(request
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(body)))
        .andExpect(status().is(expectedStatus))
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private String json(Object value) throws Exception {
    return objectMapper.writeValueAsString(value);
  }
}
