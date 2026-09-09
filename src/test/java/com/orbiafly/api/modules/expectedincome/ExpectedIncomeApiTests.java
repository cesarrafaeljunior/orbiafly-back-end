package com.orbiafly.api.modules.expectedincome;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbiafly.api.AuthenticatedUserTestConfiguration;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(AuthenticatedUserTestConfiguration.class)
class ExpectedIncomeApiTests {

  private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanFinancialData() {
    jdbcTemplate.update("DELETE FROM expected_income");
    jdbcTemplate.update("DELETE FROM loan_entry");
    jdbcTemplate.update("DELETE FROM loan_installment");
    jdbcTemplate.update("DELETE FROM loan");
    jdbcTemplate.update("DELETE FROM installment_expense_installment");
    jdbcTemplate.update("DELETE FROM installment_purchase");
    jdbcTemplate.update("DELETE FROM fixed_expense_occurrence");
    jdbcTemplate.update("DELETE FROM fixed_expense");
    jdbcTemplate.update("DELETE FROM account_transfer");
    jdbcTemplate.update("DELETE FROM financial_transaction");
    jdbcTemplate.update("DELETE FROM financial_account");
  }

  @Test
  void createsPendingIncomeWithoutChangingRealBalance() throws Exception {
    createAccount("Nubank", "DIGITAL_ACCOUNT", "Nubank", "500.00");

    JsonNode income = createIncome("Freelance", "850.00", "2026-09-25", null);

    Assertions.assertThat(income.at("/status").asText()).isEqualTo("PENDING");
    Assertions.assertThat(accounts().at("/totalAvailableBalance").decimalValue())
        .isEqualByComparingTo("500.00");
    Assertions.assertThat(expectedIncomes().at("/pendingTotal").decimalValue())
        .isEqualByComparingTo("850.00");
    Integer transactionCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM financial_transaction",
        Integer.class);
    Assertions.assertThat(transactionCount).isZero();
  }

  @Test
  void receivesIncomeOnceAndCreatesRealCredit() throws Exception {
    String accountId = createAccount("Nubank", "DIGITAL_ACCOUNT", "Nubank", "500.00")
        .get("id").asText();
    String incomeId = createIncome("Freelance", "850.00", "2026-09-25", accountId)
        .get("id").asText();

    JsonNode receipt = performJson(
        post("/api/v1/expected-incomes/{id}/receive", incomeId),
        Map.of("receivedDate", "2026-09-25"),
        200);

    Assertions.assertThat(receipt.at("/income/status").asText()).isEqualTo("RECEIVED");
    Assertions.assertThat(receipt.at("/receipt/financialAccount/currentBalance").decimalValue())
        .isEqualByComparingTo("1350.00");
    Assertions.assertThat(receipt.at("/receipt/transaction/referenceType").asText())
        .isEqualTo("EXPECTED_INCOME");

    mockMvc.perform(post("/api/v1/expected-incomes/{id}/receive", incomeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("receivedDate", "2026-09-25"))))
        .andExpect(status().isConflict());
    Integer creditCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM financial_transaction WHERE reference_type = 'EXPECTED_INCOME'",
        Integer.class);
    Assertions.assertThat(creditCount).isEqualTo(1);
    Assertions.assertThat(projection(2026, 9).at("/expectedIncomeTotal").decimalValue())
        .isEqualByComparingTo("0.00");
  }

  @Test
  void canceledIncomeDoesNotParticipateInProjection() throws Exception {
    String incomeId = createIncome("Contrato cancelado", "400.00", "2026-09-12", null)
        .get("id").asText();

    mockMvc.perform(patch("/api/v1/expected-incomes/{id}/cancel", incomeId))
        .andExpect(status().isOk());

    Assertions.assertThat(projection(2026, 9).at("/expectedIncomeTotal").decimalValue())
        .isEqualByComparingTo("0.00");
  }

  @Test
  void projectionProcessesEventsChronologicallyAndMarksCriticalRisk() throws Exception {
    createAccount("Carteira", "CASH", "", "500.00");
    createExpense("Energia", "250.00", 15);
    createExpense("Compromisso", "600.00", 20);
    createIncome("Freelance", "1000.00", "2026-09-25", null);

    JsonNode projection = projection(2026, 9);

    Assertions.assertThat(projection.at("/currentBalance").decimalValue())
        .isEqualByComparingTo("500.00");
    Assertions.assertThat(projection.at("/expectedIncomeTotal").decimalValue())
        .isEqualByComparingTo("1000.00");
    Assertions.assertThat(projection.at("/expectedExpenseTotal").decimalValue())
        .isEqualByComparingTo("850.00");
    Assertions.assertThat(projection.at("/projectedFinalBalance").decimalValue())
        .isEqualByComparingTo("650.00");
    Assertions.assertThat(projection.at("/minimumProjectedBalance").decimalValue())
        .isEqualByComparingTo("-350.00");
    Assertions.assertThat(projection.at("/minimumProjectedBalanceDate").asText())
        .isEqualTo("2026-09-20");
    Assertions.assertThat(projection.at("/healthLevel").asText()).isEqualTo("CRITICAL");
  }

  @Test
  void preventsUsingAnotherUsersAccountOrIncome() throws Exception {
    insertOtherUserAccount();

    mockMvc.perform(post("/api/v1/expected-incomes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "description", "Invasão",
                "amount", new BigDecimal("10.00"),
                "expectedDate", "2026-09-10",
                "financialAccountId", "00000000-0000-0000-0000-000000000201"))))
        .andExpect(status().isNotFound());

    jdbcTemplate.update("""
        INSERT INTO expected_income (
          id, description, amount, expected_date, status, owner_user_id, created_at, updated_at
        ) VALUES (
          '00000000-0000-0000-0000-000000000301', 'Outro usuário', 100.00, '2026-09-10',
          'PENDING', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        )
        """, OTHER_USER_ID);

    mockMvc.perform(get("/api/v1/expected-incomes/{id}", "00000000-0000-0000-0000-000000000301"))
        .andExpect(status().isNotFound());
  }

  @Test
  void updatesOnlyPendingIncome() throws Exception {
    String incomeId = createIncome("Freela", "850.00", "2026-09-25", null).get("id").asText();

    JsonNode updated = performJson(
        put("/api/v1/expected-incomes/{id}", incomeId),
        Map.of(
            "description", "Freelance ajustado",
            "amount", new BigDecimal("900.00"),
            "expectedDate", "2026-09-26",
            "notes", "Cliente confirmou"),
        200);

    Assertions.assertThat(updated.at("/description").asText()).isEqualTo("Freelance ajustado");
    Assertions.assertThat(updated.at("/amount").decimalValue()).isEqualByComparingTo("900.00");
  }

  private JsonNode createAccount(String name, String type, String institution, String openingBalance)
      throws Exception {
    return performJson(post("/api/v1/accounts"), Map.of(
        "name", name,
        "type", type,
        "institution", institution,
        "openingBalance", new BigDecimal(openingBalance)), 201);
  }

  private JsonNode createIncome(String description, String amount, String expectedDate, String accountId)
      throws Exception {
    java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
    body.put("description", description);
    body.put("amount", new BigDecimal(amount));
    body.put("expectedDate", expectedDate);
    if (accountId != null) {
      body.put("financialAccountId", accountId);
    }
    return performJson(post("/api/v1/expected-incomes"), body, 201);
  }

  private void createExpense(String name, String amount, int dueDay) throws Exception {
    performJson(post("/api/v1/fixed-expenses"), Map.of(
        "name", name,
        "defaultAmount", new BigDecimal(amount),
        "dueDay", dueDay,
        "categoryId", "00000000-0000-0000-0000-000000000001"), 201);
  }

  private JsonNode accounts() throws Exception {
    return getJson("/api/v1/accounts");
  }

  private JsonNode expectedIncomes() throws Exception {
    return getJson("/api/v1/expected-incomes");
  }

  private JsonNode projection(int year, int month) throws Exception {
    MvcResult result = mockMvc.perform(get("/api/v1/financial-projections/monthly")
            .queryParam("year", String.valueOf(year))
            .queryParam("month", String.valueOf(month)))
        .andExpect(status().isOk())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
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

  private void insertOtherUserAccount() {
    jdbcTemplate.update("""
        INSERT INTO app_user (id, email, username, password, password_hash, role, active)
        VALUES (?, 'other.expected@example.com', 'other_expected', 'x', 'x', 'ROLE_USER', TRUE)
        ON CONFLICT (id) DO NOTHING
        """, OTHER_USER_ID);
    jdbcTemplate.update("""
        INSERT INTO financial_account (
          id, name, account_type, institution, opening_balance, active, owner_user_id,
          created_at, updated_at
        ) VALUES (
          '00000000-0000-0000-0000-000000000201', 'Conta outro', 'CHECKING', 'Banco',
          100.00, TRUE, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        )
        """, OTHER_USER_ID);
  }
}
