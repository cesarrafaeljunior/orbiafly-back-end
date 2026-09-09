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
    jdbcTemplate.update("DELETE FROM expected_income");
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
    jdbcTemplate.update("DELETE FROM account_transfer");
    jdbcTemplate.update("DELETE FROM financial_transaction");
    jdbcTemplate.update("DELETE FROM financial_account");
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
    assertThat(purchase.get("purchaseType").asText()).isEqualTo("INSTALLMENT");
    assertThat(purchase.get("totalAmount").decimalValue()).isEqualByComparingTo("3000.00");
    assertThat(purchase.get("installmentAmount").decimalValue()).isEqualByComparingTo("300.00");
    assertThat(purchase.get("installments")).hasSize(10);
    assertThat(purchase.at("/installments/0/competenceYear").asInt()).isEqualTo(currentCompetence.getYear());
    assertThat(purchase.at("/installments/0/competenceMonth").asInt())
        .isEqualTo(currentCompetence.getMonthValue());
    assertThat(purchase.at("/installments/0/dueDate").asText())
        .isEqualTo(currentCompetence.atDay(15).toString());
    assertThat(purchase.get("invoiceCompetenceYear").asInt()).isEqualTo(currentCompetence.getYear());
    assertThat(purchase.get("invoiceCompetenceMonth").asInt()).isEqualTo(currentCompetence.getMonthValue());
    assertThat(purchase.get("invoiceDueDate").asText()).isEqualTo(currentCompetence.atDay(15).toString());

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
  void createsSinglePurchaseBeforeClosingDateInSameCompetenceCommitsLimitAndDebitsAccount() throws Exception {
    JsonNode card = createCard("Nubank", "MASTERCARD", "1234", 5000.00, 20, 27);
    JsonNode account = createAccount("Conta principal", "CHECKING", "Banco", "1000.00");

    JsonNode purchase = createSinglePurchase(
        card.get("id").asText(),
        account.get("id").asText(),
        "Mercado",
        "82.40",
        LocalDate.of(2026, 9, 19));

    assertThat(purchase.get("purchaseType").asText()).isEqualTo("SINGLE");
    assertThat(purchase.get("installmentCount").asInt()).isEqualTo(1);
    assertThat(purchase.get("installmentAmount").decimalValue()).isEqualByComparingTo("82.40");
    assertThat(purchase.get("invoiceCompetenceYear").asInt()).isEqualTo(2026);
    assertThat(purchase.get("invoiceCompetenceMonth").asInt()).isEqualTo(9);
    assertThat(purchase.get("invoiceDueDate").asText()).isEqualTo("2026-09-27");
    assertThat(purchase.at("/installments/0/amount").decimalValue()).isEqualByComparingTo("82.40");

    JsonNode cardsOverview = getJson("/api/v1/credit-cards");
    assertThat(cardsOverview.at("/cards/0/usedLimit").decimalValue()).isEqualByComparingTo("82.40");
    assertThat(cardsOverview.at("/cards/0/availableLimit").decimalValue()).isEqualByComparingTo("4917.60");

    JsonNode accountsOverview = getJson("/api/v1/accounts");
    assertThat(accountsOverview.at("/accounts/0/currentBalance").decimalValue()).isEqualByComparingTo("917.60");

    JsonNode transactions = getJson("/api/v1/accounts/" + account.get("id").asText() + "/transactions");
    assertThat(transactions).hasSize(1);
    assertThat(transactions.at("/0/type").asText()).isEqualTo("DEBIT");
    assertThat(transactions.at("/0/referenceType").asText()).isEqualTo("CREDIT_CARD_PURCHASE");
    assertThat(transactions.at("/0/referenceId").asText()).isEqualTo(purchase.get("id").asText());
  }

  @Test
  void createsSinglePurchaseAfterClosingDateInNextCompetenceIncludingYearTurn() throws Exception {
    JsonNode card = createCard("Nubank", "MASTERCARD", "1234", 5000.00, 20, 27);

    JsonNode account = createAccount("Conta principal", "CHECKING", "Banco", "1000.00");

    JsonNode septemberPurchase = createSinglePurchase(
        card.get("id").asText(),
        account.get("id").asText(),
        "Pão",
        "18.50",
        LocalDate.of(2026, 9, 21));
    JsonNode decemberPurchase = createSinglePurchase(
        card.get("id").asText(),
        account.get("id").asText(),
        "Ceia",
        "100.00",
        LocalDate.of(2026, 12, 31));

    assertThat(septemberPurchase.get("invoiceCompetenceYear").asInt()).isEqualTo(2026);
    assertThat(septemberPurchase.get("invoiceCompetenceMonth").asInt()).isEqualTo(10);
    assertThat(septemberPurchase.get("invoiceDueDate").asText()).isEqualTo("2026-10-27");
    assertThat(decemberPurchase.get("invoiceCompetenceYear").asInt()).isEqualTo(2027);
    assertThat(decemberPurchase.get("invoiceCompetenceMonth").asInt()).isEqualTo(1);
    assertThat(decemberPurchase.get("invoiceDueDate").asText()).isEqualTo("2027-01-27");
  }

  @Test
  void rejectsPurchaseForCardOwnedByAnotherUser() throws Exception {
    UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000101");
    UUID otherCardId = UUID.randomUUID();
    jdbcTemplate.update(
        """
        INSERT INTO app_user (id, username, email, password, password_hash, role, active)
        VALUES (?, 'outro', 'outro@orbiafly.test', 'legacy-password', 'password-hash', 'ROLE_USER', TRUE)
        ON CONFLICT (id) DO NOTHING
        """,
        otherUserId);
    jdbcTemplate.update(
        """
        INSERT INTO credit_card (
          id, name, brand, total_limit, closing_day, due_day, active, owner_user_id
        ) VALUES (?, 'Outro cartão', 'VISA', 1000.00, 20, 27, TRUE, ?)
        """,
        otherCardId,
        otherUserId);

    mockMvc.perform(post("/api/v1/credit-card-purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(purchasePayload(
                otherCardId.toString(),
                "Compra indevida",
                "10.00",
                LocalDate.of(2026, 9, 21),
                "SINGLE",
                UUID.randomUUID().toString(),
                null))))
        .andExpect(status().isNotFound());
  }

  @Test
  void allowsPurchaseAboveLimitAndKeepsAvailableLimitNegative() throws Exception {
    JsonNode card = createCard("XP", "VISA", "4567", 1000.00, 20, 5);
    String cardId = card.get("id").asText();

    createPurchase(cardId, "Celular", "1500.00", LocalDate.of(2027, 2, 1), 3);

    JsonNode cardsOverview = getJson("/api/v1/credit-cards");
    assertThat(cardsOverview.at("/cards/0/usedLimit").decimalValue()).isEqualByComparingTo("1500.00");
    assertThat(cardsOverview.at("/cards/0/availableLimit").decimalValue()).isEqualByComparingTo("-500.00");
  }

  @Test
  void rejectsPurchaseWhenCardIsInactive() throws Exception {
    JsonNode card = createCard("XP", "VISA", "4567", 1000.00, 20, 5);
    String cardId = card.get("id").asText();

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

  private JsonNode createAccount(
      String name,
      String type,
      String institution,
      String openingBalance) throws Exception {
    Map<String, Object> payload = new HashMap<>();
    payload.put("name", name);
    payload.put("type", type);
    payload.put("institution", institution);
    payload.put("openingBalance", openingBalance);
    MvcResult result = mockMvc.perform(post("/api/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(payload)))
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
    return createPurchase(cardId, description, totalAmount, purchaseDate, "INSTALLMENT", null, installmentCount);
  }

  private JsonNode createSinglePurchase(
      String cardId,
      String accountId,
      String description,
      String totalAmount,
      LocalDate purchaseDate) throws Exception {
    return createPurchase(cardId, description, totalAmount, purchaseDate, "SINGLE", accountId, null);
  }

  private JsonNode createPurchase(
      String cardId,
      String description,
      String totalAmount,
      LocalDate purchaseDate,
      String purchaseType,
      String financialAccountId,
      Integer installmentCount) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/credit-card-purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(purchasePayload(
                cardId,
                description,
                totalAmount,
                purchaseDate,
                purchaseType,
                financialAccountId,
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
    return purchasePayload(cardId, description, totalAmount, purchaseDate, "INSTALLMENT", null, installmentCount);
  }

  private Map<String, Object> purchasePayload(
      String cardId,
      String description,
      String totalAmount,
      LocalDate purchaseDate,
      String purchaseType,
      String financialAccountId,
      Integer installmentCount) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("description", description);
    payload.put("creditCardId", UUID.fromString(cardId));
    payload.put("totalAmount", totalAmount);
    payload.put("purchaseDate", purchaseDate.toString());
    payload.put("purchaseType", purchaseType);
    if (financialAccountId != null) {
      payload.put("financialAccountId", UUID.fromString(financialAccountId));
    }
    if (installmentCount != null) {
      payload.put("installmentCount", installmentCount);
    }
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
