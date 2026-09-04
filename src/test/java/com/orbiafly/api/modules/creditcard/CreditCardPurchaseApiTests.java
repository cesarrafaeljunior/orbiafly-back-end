package com.orbiafly.api.modules.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbiafly.api.AuthenticatedUserTestConfiguration;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(AuthenticatedUserTestConfiguration.class)
class CreditCardPurchaseApiTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private Clock clock;

  @BeforeEach
  void cleanCreditCardPurchases() {
    jdbcTemplate.update("DELETE FROM credit_card_installment");
    jdbcTemplate.update("DELETE FROM credit_card_purchase");
    jdbcTemplate.update("DELETE FROM credit_card");
  }

  @Test
  void createsPurchaseInstallmentsAndCommitsFullAmountToCardLimit() throws Exception {
    YearMonth currentCompetence = YearMonth.now(clock);
    JsonNode card = createCard("Nubank", "MASTERCARD", "1234", 5000.00, 28, 15);

    JsonNode purchase = createPurchase(
        card.get("id").asText(),
        "Notebook",
        "3000.00",
        currentCompetence.atDay(1),
        10);

    assertThat(purchase.get("description").asText()).isEqualTo("Notebook");
    assertThat(purchase.get("status").asText()).isEqualTo("EM_ANDAMENTO");
    assertThat(purchase.get("totalAmount").decimalValue()).isEqualByComparingTo("3000.00");
    assertThat(purchase.get("installmentAmount").decimalValue()).isEqualByComparingTo("300.00");
    assertThat(purchase.get("installments")).hasSize(10);
    assertThat(purchase.at("/installments/0/competenceYear").asInt()).isEqualTo(currentCompetence.getYear());
    assertThat(purchase.at("/installments/0/competenceMonth").asInt())
        .isEqualTo(currentCompetence.getMonthValue());
    assertThat(purchase.at("/installments/0/dueDate").asText())
        .isEqualTo(currentCompetence.atDay(15).toString());

    JsonNode cardsOverview = getJson("/api/v1/credit-cards");
    assertThat(cardsOverview.at("/cards/0/usedLimit").decimalValue()).isEqualByComparingTo("3000.00");
    assertThat(cardsOverview.at("/cards/0/availableLimit").decimalValue()).isEqualByComparingTo("2000.00");

    mockMvc.perform(get("/api/v1/credit-card-purchases"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCommitted").value(3000.00))
        .andExpect(jsonPath("$.activePurchaseCount").value(1))
        .andExpect(jsonPath("$.futureInstallmentCount").value(10))
        .andExpect(jsonPath("$.currentMonthInstallmentsAmount").value(300.00))
        .andExpect(jsonPath("$.purchases", hasSize(1)));
  }

  @Test
  void adjustsLastInstallmentToPreserveTotalAmount() throws Exception {
    JsonNode card = createCard("Inter", "VISA", "", 1000.00, 28, 10);
    YearMonth currentCompetence = YearMonth.now(clock);

    JsonNode purchase = createPurchase(
        card.get("id").asText(),
        "Curso",
        "100.00",
        currentCompetence.atDay(1),
        3);

    assertThat(purchase.at("/installments/0/amount").decimalValue()).isEqualByComparingTo("33.33");
    assertThat(purchase.at("/installments/1/amount").decimalValue()).isEqualByComparingTo("33.33");
    assertThat(purchase.at("/installments/2/amount").decimalValue()).isEqualByComparingTo("33.34");
    BigDecimal storedTotal = jdbcTemplate.queryForObject(
        "SELECT COALESCE(SUM(amount), 0) FROM credit_card_installment",
        BigDecimal.class);
    assertThat(storedTotal).isEqualByComparingTo("100.00");
  }

  @Test
  void startsInstallmentsOnFollowingCompetenceAfterCardClosingDate() throws Exception {
    JsonNode card = createCard("C6", "ELO", "9999", 2000.00, 10, 31);

    JsonNode purchase = createPurchase(
        card.get("id").asText(),
        "Passagem",
        "600.00",
        LocalDate.of(2027, 1, 11),
        2);

    assertThat(purchase.at("/installments/0/competenceYear").asInt()).isEqualTo(2027);
    assertThat(purchase.at("/installments/0/competenceMonth").asInt()).isEqualTo(2);
    assertThat(purchase.at("/installments/0/dueDate").asText()).isEqualTo("2027-02-28");
    assertThat(purchase.at("/installments/1/competenceMonth").asInt()).isEqualTo(3);
    assertThat(purchase.at("/installments/1/dueDate").asText()).isEqualTo("2027-03-31");
  }

  @Test
  void rejectsPurchaseWhenCardIsInactiveOrLimitIsInsufficient() throws Exception {
    JsonNode card = createCard("XP", "VISA", "4567", 1000.00, 20, 5);
    String cardId = card.get("id").asText();

    mockMvc.perform(post("/api/v1/credit-card-purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(purchasePayload(cardId, "Celular", "1500.00", LocalDate.of(2027, 2, 1), 3))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Limite disponível insuficiente para registrar a compra."));

    mockMvc.perform(patch("/api/v1/credit-cards/{id}/deactivate", cardId))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/credit-card-purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(purchasePayload(cardId, "Fone", "100.00", LocalDate.of(2027, 2, 1), 2))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("O cartão selecionado está inativo."));
  }

  @Test
  void softDeletesPurchaseAndReleasesCommittedLimit() throws Exception {
    JsonNode card = createCard("Nubank", "MASTERCARD", "1234", 1000.00, 28, 15);
    JsonNode purchase = createPurchase(
        card.get("id").asText(),
        "Monitor",
        "900.00",
        YearMonth.now(clock).atDay(1),
        3);

    mockMvc.perform(delete("/api/v1/credit-card-purchases/{id}", purchase.get("id").asText()))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/credit-card-purchases"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.purchases", hasSize(0)))
        .andExpect(jsonPath("$.totalCommitted").value(0));

    JsonNode cardsOverview = getJson("/api/v1/credit-cards");
    assertThat(cardsOverview.at("/cards/0/usedLimit").decimalValue()).isEqualByComparingTo("0.00");
    assertThat(cardsOverview.at("/cards/0/availableLimit").decimalValue()).isEqualByComparingTo("1000.00");

    Integer deletedPurchases = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM credit_card_purchase WHERE deleted_at IS NOT NULL",
        Integer.class);
    assertThat(deletedPurchases).isEqualTo(1);
  }

  private JsonNode createCard(
      String name,
      String brand,
      String lastFourDigits,
      double totalLimit,
      int closingDay,
      int dueDay) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/credit-cards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(cardPayload(name, brand, lastFourDigits, totalLimit, closingDay, dueDay))))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private JsonNode createPurchase(
      String cardId,
      String description,
      String totalAmount,
      LocalDate purchaseDate,
      int installmentCount) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/credit-card-purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(purchasePayload(
                cardId,
                description,
                totalAmount,
                purchaseDate,
                installmentCount))))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
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

  private Map<String, Object> purchasePayload(
      String cardId,
      String description,
      String totalAmount,
      LocalDate purchaseDate,
      int installmentCount) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("description", description);
    payload.put("creditCardId", UUID.fromString(cardId));
    payload.put("totalAmount", totalAmount);
    payload.put("purchaseDate", purchaseDate.toString());
    payload.put("installmentCount", installmentCount);
    return payload;
  }

  private JsonNode getJson(String path) throws Exception {
    MvcResult result = mockMvc.perform(get(path))
        .andExpect(status().isOk())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private String json(Object value) throws Exception {
    return objectMapper.writeValueAsString(value);
  }
}
