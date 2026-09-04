package com.orbiafly.api.modules.fixedexpense;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
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
class FixedExpenseApiTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanExpenses() {
    jdbcTemplate.update("DELETE FROM installment_expense_installment");
    jdbcTemplate.update("DELETE FROM installment_purchase");
    jdbcTemplate.update("DELETE FROM fixed_expense_occurrence");
    jdbcTemplate.update("DELETE FROM fixed_expense");
    jdbcTemplate.update("DELETE FROM financial_transaction");
    jdbcTemplate.update("DELETE FROM financial_account");
  }

  @Test
  void exposesPersistedCategoriesAndValidatesCreation() throws Exception {
    mockMvc.perform(get("/api/v1/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(11)))
        .andExpect(jsonPath("$[0].id", notNullValue()));

    mockMvc.perform(post("/api/v1/fixed-expenses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "name", " ",
                "defaultAmount", -1,
                "dueDay", 32,
                "categoryId", "00000000-0000-0000-0000-000000000004"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Dados inválidos"))
        .andExpect(jsonPath("$.violations.name").value("Informe o nome."))
        .andExpect(jsonPath("$.violations.defaultAmount").value("O valor não pode ser negativo."))
        .andExpect(jsonPath("$.violations.dueDay")
            .value("O vencimento deve estar entre os dias 1 e 31."));
  }

  @Test
  void managesMonthlyLifecycleWithoutResettingOrLosingHistory() throws Exception {
    JsonNode created = performJson(post("/api/v1/fixed-expenses"), Map.of(
        "name", "Energia",
        "defaultAmount", 200.00,
        "dueDay", 18,
        "categoryId", "00000000-0000-0000-0000-000000000004"), 201);
    String fixedExpenseId = created.get("id").asText();

    JsonNode august = getMonth(2026, 8);
    String augustOccurrenceId = august.at("/items/0/occurrenceId").asText();
    org.assertj.core.api.Assertions.assertThat(august.at("/items/0/amount").decimalValue())
        .isEqualByComparingTo("200.00");
    org.assertj.core.api.Assertions.assertThat(august.at("/items/0/status").asText())
        .isEqualTo("PENDING");
    org.assertj.core.api.Assertions.assertThat(august.at("/items/0/dueDate").asText())
        .isEqualTo("2026-08-18");
    org.assertj.core.api.Assertions.assertThat(august.at("/items/0/overdue").asBoolean())
        .isTrue();
    org.assertj.core.api.Assertions.assertThat(august.at("/summary/totalCount").asInt())
        .isEqualTo(1);
    org.assertj.core.api.Assertions.assertThat(august.at("/summary/paidPercentage").decimalValue())
        .isEqualByComparingTo("0.00");

    getMonth(2026, 8);
    Integer occurrenceCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM fixed_expense_occurrence WHERE occurrence_year = 2026 AND occurrence_month = 8",
        Integer.class);
    org.assertj.core.api.Assertions.assertThat(occurrenceCount).isEqualTo(1);

    performJson(
        patch("/api/v1/fixed-expense-occurrences/{id}/amount", augustOccurrenceId),
        Map.of("amount", 247.82),
        200);
    JsonNode paid = performJson(
        patch("/api/v1/fixed-expense-occurrences/{id}/status", augustOccurrenceId),
        Map.of("status", "PAID"),
        200);
    org.assertj.core.api.Assertions.assertThat(paid.get("paidAt").isNull()).isFalse();

    JsonNode dashboard = getDashboard(2026, 8);
    org.assertj.core.api.Assertions.assertThat(dashboard.at("/summary/totalAmount").decimalValue())
        .isEqualByComparingTo("247.82");
    org.assertj.core.api.Assertions.assertThat(dashboard.at("/summary/paidAmount").decimalValue())
        .isEqualByComparingTo("247.82");
    org.assertj.core.api.Assertions.assertThat(dashboard.at("/summary/paidPercentage").decimalValue())
        .isEqualByComparingTo("100.00");
    org.assertj.core.api.Assertions.assertThat(dashboard.at("/overdue/count").asInt()).isZero();
    org.assertj.core.api.Assertions.assertThat(dashboard.get("largestExpenses").size()).isOne();

    performJson(put("/api/v1/fixed-expenses/{id}", fixedExpenseId), Map.of(
        "name", "Energia residencial",
        "defaultAmount", 300.00,
        "dueDay", 20,
        "categoryId", "00000000-0000-0000-0000-000000000001",
        "active", true), 200);

    JsonNode augustHistory = getMonth(2026, 8);
    org.assertj.core.api.Assertions.assertThat(augustHistory.at("/items/0/name").asText())
        .isEqualTo("Energia");
    org.assertj.core.api.Assertions.assertThat(augustHistory.at("/items/0/category").asText())
        .isEqualTo("Casa");
    org.assertj.core.api.Assertions.assertThat(augustHistory.at("/items/0/dueDay").asInt())
        .isEqualTo(18);
    org.assertj.core.api.Assertions.assertThat(augustHistory.at("/items/0/amount").decimalValue())
        .isEqualByComparingTo("247.82");
    org.assertj.core.api.Assertions.assertThat(augustHistory.at("/summary/paidTotal").decimalValue())
        .isEqualByComparingTo("247.82");
    org.assertj.core.api.Assertions.assertThat(augustHistory.at("/summary/pendingTotal").decimalValue())
        .isEqualByComparingTo("0.00");
    org.assertj.core.api.Assertions.assertThat(augustHistory.at("/summary/paidPercentage").decimalValue())
        .isEqualByComparingTo("100.00");

    JsonNode september = getMonth(2026, 9);
    String septemberOccurrenceId = september.at("/items/0/occurrenceId").asText();
    org.assertj.core.api.Assertions.assertThat(september.at("/items/0/name").asText())
        .isEqualTo("Energia residencial");
    org.assertj.core.api.Assertions.assertThat(september.at("/items/0/amount").decimalValue())
        .isEqualByComparingTo("300.00");
    org.assertj.core.api.Assertions.assertThat(september.at("/items/0/status").asText())
        .isEqualTo("PENDING");

    performJson(
        patch("/api/v1/fixed-expense-occurrences/{id}/status", septemberOccurrenceId),
        Map.of("status", "PAID"),
        200);
    JsonNode pendingAgain = performJson(
        patch("/api/v1/fixed-expense-occurrences/{id}/status", septemberOccurrenceId),
        Map.of("status", "PENDING"),
        200);
    org.assertj.core.api.Assertions.assertThat(pendingAgain.get("paidAt").isNull()).isTrue();

    performJson(put("/api/v1/fixed-expenses/{id}", fixedExpenseId), Map.of(
        "name", "Energia residencial",
        "defaultAmount", 300.00,
        "dueDay", 20,
        "categoryId", "00000000-0000-0000-0000-000000000001",
        "active", false), 200);

    JsonNode october = getMonth(2026, 10);
    org.assertj.core.api.Assertions.assertThat(october.get("items").size()).isZero();
    org.assertj.core.api.Assertions.assertThat(october.at("/summary/total").decimalValue())
        .isEqualByComparingTo("0.00");

    JsonNode preservedAugust = getMonth(2026, 8);
    org.assertj.core.api.Assertions.assertThat(preservedAugust.get("items").size()).isOne();
    org.assertj.core.api.Assertions.assertThat(preservedAugust.at("/items/0/active").asBoolean())
        .isFalse();
  }

  @Test
  void rejectsInvalidPeriodAndMonthlyAmount() throws Exception {
    mockMvc.perform(get("/api/v1/fixed-expenses/monthly")
            .queryParam("year", "2026")
            .queryParam("month", "13"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Parâmetros inválidos"));

    mockMvc.perform(get("/api/v1/dashboard/monthly")
            .queryParam("year", "1999")
            .queryParam("month", "1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Parâmetros inválidos"));

    mockMvc.perform(patch(
            "/api/v1/fixed-expense-occurrences/{id}/amount",
            "00000000-0000-0000-0000-000000000099")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("amount", -0.01))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.violations.amount").value("O valor não pode ser negativo."));
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
