package com.orbiafly.api.modules.creditcard;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbiafly.api.support.TestCurrentUserConfig;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@Import(TestCurrentUserConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CreditCardApiTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanCreditCards() {
    jdbcTemplate.update("DELETE FROM installment_expense_installment");
    jdbcTemplate.update("DELETE FROM installment_purchase");
    jdbcTemplate.update("DELETE FROM credit_card");
  }

  @Test
  void managesCreditCardsLifecycleAndDerivedLimits() throws Exception {
    JsonNode nubank = createCard("Nubank", "MASTERCARD", "1234", 5000.00, 3, 10);
    createCard("Inter", "VISA", "", 2500.00, 8, 15);

    Assertions.assertThat(nubank.at("/usedLimit").decimalValue()).isEqualByComparingTo("0.00");
    Assertions.assertThat(nubank.at("/availableLimit").decimalValue()).isEqualByComparingTo("5000.00");
    Assertions.assertThat(nubank.at("/deletedAt").isNull()).isTrue();

    JsonNode overview = getJson("/api/v1/credit-cards");
    Assertions.assertThat(overview.at("/totalLimit").decimalValue()).isEqualByComparingTo("7500.00");
    Assertions.assertThat(overview.at("/usedLimit").decimalValue()).isEqualByComparingTo("0.00");
    Assertions.assertThat(overview.at("/availableLimit").decimalValue()).isEqualByComparingTo("7500.00");
    Assertions.assertThat(overview.at("/activeCount").asInt()).isEqualTo(2);

    String nubankId = nubank.get("id").asText();
    mockMvc.perform(put("/api/v1/credit-cards/{id}", nubankId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(cardPayload("Cartão roxo", "ELO", "4321", 6400.00, 4, 12))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Cartão roxo"))
        .andExpect(jsonPath("$.brand").value("ELO"))
        .andExpect(jsonPath("$.lastFourDigits").value("4321"))
        .andExpect(jsonPath("$.totalLimit").value(6400.00))
        .andExpect(jsonPath("$.usedLimit").value(0))
        .andExpect(jsonPath("$.availableLimit").value(6400.00));

    mockMvc.perform(patch("/api/v1/credit-cards/{id}/deactivate", nubankId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));

    mockMvc.perform(get("/api/v1/credit-cards"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cards", hasSize(1)))
        .andExpect(jsonPath("$.totalLimit").value(2500.00))
        .andExpect(jsonPath("$.activeCount").value(1));

    mockMvc.perform(get("/api/v1/credit-cards").queryParam("includeInactive", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cards", hasSize(2)));

    mockMvc.perform(patch("/api/v1/credit-cards/{id}/reactivate", nubankId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(true));

    mockMvc.perform(delete("/api/v1/credit-cards/{id}", nubankId))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/credit-cards").queryParam("includeInactive", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cards", hasSize(1)))
        .andExpect(jsonPath("$.totalLimit").value(2500.00));
    mockMvc.perform(get("/api/v1/credit-cards/{id}", nubankId))
        .andExpect(status().isNotFound());

    Integer storedCards = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM credit_card",
        Integer.class);
    Integer deletedCards = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM credit_card WHERE deleted_at IS NOT NULL",
        Integer.class);
    Assertions.assertThat(storedCards).isEqualTo(2);
    Assertions.assertThat(deletedCards).isEqualTo(1);
  }

  @Test
  void rejectsInvalidLastFourDigitsAndNegativeLimit() throws Exception {
    mockMvc.perform(post("/api/v1/credit-cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(cardPayload("Nubank", "MASTERCARD", "12A4", 5000.00, 3, 10))))
        .andExpect(status().isBadRequest());

    mockMvc.perform(post("/api/v1/credit-cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(cardPayload("Nubank", "MASTERCARD", "1234", -1.00, 3, 10))))
        .andExpect(status().isBadRequest());
  }

  private JsonNode createCard(
      String name,
      String brand,
      String lastFourDigits,
      double totalLimit,
      int closingDay,
      int dueDay) throws Exception {
    return performJson(
        post("/api/v1/credit-cards"),
        cardPayload(name, brand, lastFourDigits, totalLimit, closingDay, dueDay),
        201);
  }

  private Map<String, Object> cardPayload(
      String name,
      String brand,
      String lastFourDigits,
      double totalLimit,
      int closingDay,
      int dueDay) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("name", name);
    payload.put("brand", brand);
    payload.put("lastFourDigits", lastFourDigits);
    payload.put("totalLimit", totalLimit);
    payload.put("closingDay", closingDay);
    payload.put("dueDay", dueDay);
    return payload;
  }

  private JsonNode getJson(String path) throws Exception {
    MvcResult result = mockMvc.perform(get(path))
        .andExpect(status().isOk())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
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
