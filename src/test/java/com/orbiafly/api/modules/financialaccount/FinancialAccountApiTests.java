package com.orbiafly.api.modules.financialaccount;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbiafly.api.AuthenticatedUserTestConfiguration;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(AuthenticatedUserTestConfiguration.class)
class FinancialAccountApiTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanFinancialData() {
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
  void managesAccountsDerivedBalancesAdjustmentsAndDeactivation() throws Exception {
    JsonNode caixa = createAccount("Conta Caixa", "CHECKING", "Caixa", 2500.00);
    createAccount("Conta Nubank", "DIGITAL_ACCOUNT", "Nubank", 1200.00);

    JsonNode overview = getJson("/api/v1/accounts");
    Assertions.assertThat(overview.at("/totalAvailableBalance").decimalValue())
        .isEqualByComparingTo("3700.00");
    Assertions.assertThat(overview.at("/activeCount").asInt()).isEqualTo(2);

    String caixaId = caixa.get("id").asText();
    JsonNode adjustment = performJson(
        post("/api/v1/accounts/{id}/adjustments", caixaId),
        Map.of("targetBalance", 2430.00, "description", "Conciliação manual"),
        200);
    Assertions.assertThat(adjustment.at("/financialAccount/currentBalance").decimalValue())
        .isEqualByComparingTo("2430.00");
    Assertions.assertThat(adjustment.at("/transaction/type").asText())
        .isEqualTo("ADJUSTMENT");
    Assertions.assertThat(adjustment.at("/transaction/balanceEffect").decimalValue())
        .isEqualByComparingTo("-70.00");

    mockMvc.perform(put("/api/v1/accounts/{id}", caixaId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "name", "Caixa principal",
                "type", "SAVINGS",
                "institution", "Caixa Econômica"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openingBalance").value(2500.00))
        .andExpect(jsonPath("$.currentBalance").value(2430.00));

    mockMvc.perform(get("/api/v1/accounts/{id}/transactions", caixaId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].description").value("Conciliação manual"));

    mockMvc.perform(patch("/api/v1/accounts/{id}/deactivate", caixaId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false))
        .andExpect(jsonPath("$.currentBalance").value(2430.00));

    mockMvc.perform(get("/api/v1/accounts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accounts", hasSize(1)))
        .andExpect(jsonPath("$.totalAvailableBalance").value(1200.00));

    mockMvc.perform(get("/api/v1/accounts").queryParam("includeInactive", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accounts", hasSize(2)));

    mockMvc.perform(patch("/api/v1/accounts/{id}/reactivate", caixaId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(true))
        .andExpect(jsonPath("$.currentBalance").value(2430.00));

    mockMvc.perform(get("/api/v1/accounts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accounts", hasSize(2)))
        .andExpect(jsonPath("$.totalAvailableBalance").value(3630.00));

    mockMvc.perform(get("/api/v1/accounts/{id}/transactions", caixaId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void paysIdempotentlyTracksSourceAndReversesFinancially() throws Exception {
    JsonNode expense = createExpense("Aluguel", 900.00, 5);
    String occurrenceId = getMonth(2026, 8).at("/items/0/occurrenceId").asText();

    mockMvc.perform(patch("/api/v1/fixed-expense-occurrences/{id}/status", occurrenceId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("status", "PAID"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.financialAccountId").doesNotExist());
    mockMvc.perform(patch("/api/v1/fixed-expense-occurrences/{id}/status", occurrenceId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("status", "PENDING"))))
        .andExpect(status().isOk());

    JsonNode caixa = createAccount("Conta Caixa", "CHECKING", "Caixa", 2500.00);
    createAccount("Conta Nubank", "DIGITAL_ACCOUNT", "Nubank", 1200.00);
    String caixaId = caixa.get("id").asText();
    Map<String, Object> paymentBody = Map.of(
        "financialAccountId", caixaId,
        "paymentDate", "2026-08-25");

    JsonNode payment = performJson(
        post("/api/v1/fixed-expense-occurrences/{id}/payment", occurrenceId),
        paymentBody,
        200);
    String transactionId = payment.at("/payment/id").asText();
    Assertions.assertThat(payment.at("/updatedBalance").decimalValue())
        .isEqualByComparingTo("1600.00");
    Assertions.assertThat(payment.at("/occurrence/status").asText()).isEqualTo("PAID");
    Assertions.assertThat(payment.at("/occurrence/financialAccountName").asText())
        .isEqualTo("Conta Caixa");
    Assertions.assertThat(payment.at("/payment/balanceEffect").decimalValue())
        .isEqualByComparingTo("-900.00");

    JsonNode retry = performJson(
        post("/api/v1/fixed-expense-occurrences/{id}/payment", occurrenceId),
        paymentBody,
        200);
    Assertions.assertThat(retry.at("/payment/id").asText()).isEqualTo(transactionId);
    Integer debitCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM financial_transaction WHERE transaction_type = 'DEBIT'",
        Integer.class);
    Assertions.assertThat(debitCount).isEqualTo(1);

    JsonNode dashboard = getDashboard(2026, 8);
    Assertions.assertThat(dashboard.at("/accounts/availableBalance").decimalValue())
        .isEqualByComparingTo("2800.00");

    mockMvc.perform(patch("/api/v1/fixed-expense-occurrences/{id}/status", occurrenceId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("status", "PENDING"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.financialAccountId").doesNotExist());

    JsonNode restored = getJson("/api/v1/accounts/" + caixaId);
    Assertions.assertThat(restored.get("currentBalance").decimalValue())
        .isEqualByComparingTo("2500.00");
    mockMvc.perform(get("/api/v1/accounts/{id}/transactions", caixaId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].type").value("CREDIT"))
        .andExpect(jsonPath("$[0].reversalOfId").value(transactionId));

    Assertions.assertThat(expense.get("id").asText()).isNotBlank();
  }

  @Test
  void rejectsInactiveAccountAndRollsBackInvalidPayment() throws Exception {
    JsonNode account = createAccount("Carteira", "CASH", "", 150.00);
    String accountId = account.get("id").asText();
    mockMvc.perform(patch("/api/v1/accounts/{id}/deactivate", accountId))
        .andExpect(status().isOk());
    createExpense("Internet", 100.00, 10);
    String occurrenceId = getMonth(2026, 8).at("/items/0/occurrenceId").asText();

    mockMvc.perform(post("/api/v1/fixed-expense-occurrences/{id}/payment", occurrenceId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "financialAccountId", accountId,
                "paymentDate", "2026-08-25"))))
        .andExpect(status().isConflict());

    JsonNode month = getMonth(2026, 8);
    Assertions.assertThat(month.at("/items/0/status").asText()).isEqualTo("PENDING");
    Integer transactionCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM financial_transaction",
        Integer.class);
    Assertions.assertThat(transactionCount).isZero();
  }

  @Test
  void deletesInactiveAccountTransactionsAndPaymentLinks() throws Exception {
    createExpense("Internet", 100.00, 10);
    String occurrenceId = getMonth(2026, 8).at("/items/0/occurrenceId").asText();
    JsonNode account = createAccount("Carteira", "CASH", "", 150.00);
    String accountId = account.get("id").asText();

    performJson(
        post("/api/v1/fixed-expense-occurrences/{id}/payment", occurrenceId),
        Map.of("financialAccountId", accountId, "paymentDate", "2026-08-25"),
        200);
    mockMvc.perform(patch("/api/v1/accounts/{id}/deactivate", accountId))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/api/v1/accounts/{id}", accountId))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/accounts").queryParam("includeInactive", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accounts", hasSize(0)))
        .andExpect(jsonPath("$.totalAvailableBalance").value(0));
    mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
        .andExpect(status().isNotFound());

    JsonNode month = getMonth(2026, 8);
    Assertions.assertThat(month.at("/items/0/status").asText()).isEqualTo("PENDING");
    JsonNode financialAccountId = month.at("/items/0/financialAccountId");
    Assertions.assertThat(financialAccountId.isMissingNode() || financialAccountId.isNull())
        .isTrue();
    Assertions.assertThat(month.at("/summary/pendingTotal").decimalValue())
        .isEqualByComparingTo("100.00");

    Integer accountCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM financial_account",
        Integer.class);
    Integer transactionCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM financial_transaction",
        Integer.class);
    Assertions.assertThat(accountCount).isZero();
    Assertions.assertThat(transactionCount).isZero();
  }

  @Test
  void allowsDeleteAccountPreflightFromLocalFrontend() throws Exception {
    mockMvc.perform(options("/api/v1/accounts/{id}", "00000000-0000-0000-0000-000000000001")
            .header("Origin", "http://127.0.0.1:4201")
            .header("Access-Control-Request-Method", "DELETE"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:4201"))
        .andExpect(header().string("Access-Control-Allow-Methods", containsString("DELETE")));
  }

  private JsonNode createAccount(String name, String type, String institution, double openingBalance)
      throws Exception {
    return performJson(post("/api/v1/accounts"), Map.of(
        "name", name,
        "type", type,
        "institution", institution,
        "openingBalance", openingBalance), 201);
  }

  private JsonNode createExpense(String name, double amount, int dueDay) throws Exception {
    return performJson(post("/api/v1/fixed-expenses"), Map.of(
        "name", name,
        "defaultAmount", amount,
        "dueDay", dueDay,
        "categoryId", "00000000-0000-0000-0000-000000000001"), 201);
  }

  private JsonNode getMonth(int year, int month) throws Exception {
    MvcResult result = mockMvc.perform(get("/api/v1/fixed-expenses/monthly")
            .queryParam("year", String.valueOf(year))
            .queryParam("month", String.valueOf(month)))
        .andExpect(status().isOk())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private JsonNode getDashboard(int year, int month) throws Exception {
    MvcResult result = mockMvc.perform(get("/api/v1/dashboard/monthly")
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
}
