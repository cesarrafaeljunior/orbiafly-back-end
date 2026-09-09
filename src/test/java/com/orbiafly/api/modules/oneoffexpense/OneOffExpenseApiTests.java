package com.orbiafly.api.modules.oneoffexpense;

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
import com.orbiafly.api.AuthenticatedUserTestConfiguration;
import java.util.Map;
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
class OneOffExpenseApiTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanFinancialData() {
    jdbcTemplate.update("TRUNCATE TABLE one_off_expense, loan_entry, loan_installment, loan, "
        + "installment_expense_installment, installment_purchase, fixed_expense_occurrence, "
        + "fixed_expense, account_transfer, expected_income, credit_card_installment, "
        + "credit_card_purchase, credit_card, financial_transaction, financial_account CASCADE");
  }

  @Test
  void managesMonthlyExpensesAndSummary() throws Exception {
    JsonNode created = performJson(post("/api/v1/one-off-expenses"), Map.of(
        "name", "Manutenção da bicicleta",
        "amount", 180.50,
        "expenseDate", "2026-09-12",
        "categoryId", "00000000-0000-0000-0000-000000000006"), 201);

    String id = created.get("id").asText();
    mockMvc.perform(get("/api/v1/one-off-expenses")
            .queryParam("year", "2026").queryParam("month", "9"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].name").value("Manutenção da bicicleta"))
        .andExpect(jsonPath("$.summary.total").value(180.50))
        .andExpect(jsonPath("$.summary.pendingCount").value(1));

    mockMvc.perform(put("/api/v1/one-off-expenses/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "name", "Manutenção completa",
                "amount", 220.00,
                "expenseDate", "2026-10-03",
                "categoryId", "00000000-0000-0000-0000-000000000006"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Manutenção completa"));

    mockMvc.perform(get("/api/v1/one-off-expenses")
            .queryParam("year", "2026").queryParam("month", "9"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.items", hasSize(0)));

    mockMvc.perform(delete("/api/v1/one-off-expenses/{id}", id))
        .andExpect(status().isNoContent());
  }

  @Test
  void paysIdempotentlyAndReversesAccountDebit() throws Exception {
    JsonNode account = performJson(post("/api/v1/accounts"), Map.of(
        "name", "Carteira", "type", "CASH", "institution", "", "openingBalance", 500.00), 201);
    JsonNode expense = performJson(post("/api/v1/one-off-expenses"), Map.of(
        "name", "Presente", "amount", 120.00, "expenseDate", "2026-09-09",
        "categoryId", "00000000-0000-0000-0000-000000000009"), 201);
    String accountId = account.get("id").asText();
    String expenseId = expense.get("id").asText();
    Map<String, Object> payment = Map.of(
        "financialAccountId", accountId, "paymentDate", "2026-09-09");

    JsonNode first = performJson(post("/api/v1/one-off-expenses/{id}/payment", expenseId), payment, 200);
    JsonNode second = performJson(post("/api/v1/one-off-expenses/{id}/payment", expenseId), payment, 200);
    Assertions.assertThat(first.at("/updatedBalance").decimalValue()).isEqualByComparingTo("380.00");
    Assertions.assertThat(second.at("/payment/id").asText()).isEqualTo(first.at("/payment/id").asText());

    mockMvc.perform(patch("/api/v1/one-off-expenses/{id}/status", expenseId)
            .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "PENDING"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.financialAccountId").doesNotExist());

    MvcResult result = mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
        .andExpect(status().isOk()).andReturn();
    Assertions.assertThat(objectMapper.readTree(result.getResponse().getContentAsString())
        .at("/currentBalance").decimalValue()).isEqualByComparingTo("500.00");
    Assertions.assertThat(jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM financial_transaction", Integer.class)).isEqualTo(2);
  }

  @Test
  void validatesPayloadAndRequiresPendingStateForChanges() throws Exception {
    mockMvc.perform(post("/api/v1/one-off-expenses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "name", " ", "amount", 0, "expenseDate", "2026-09-09",
                "categoryId", "00000000-0000-0000-0000-000000000001"))))
        .andExpect(status().isBadRequest());

    JsonNode expense = performJson(post("/api/v1/one-off-expenses"), Map.of(
        "name", "Taxa", "amount", 25.00, "expenseDate", "2026-09-09",
        "categoryId", "00000000-0000-0000-0000-000000000001"), 201);
    String id = expense.get("id").asText();
    mockMvc.perform(patch("/api/v1/one-off-expenses/{id}/status", id)
            .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "PAID"))))
        .andExpect(status().isOk());
    mockMvc.perform(delete("/api/v1/one-off-expenses/{id}", id))
        .andExpect(status().isConflict());
  }

  private JsonNode performJson(
      org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
      Object body,
      int expectedStatus) throws Exception {
    MvcResult result = mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(json(body)))
        .andExpect(status().is(expectedStatus)).andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private String json(Object value) throws Exception {
    return objectMapper.writeValueAsString(value);
  }
}
