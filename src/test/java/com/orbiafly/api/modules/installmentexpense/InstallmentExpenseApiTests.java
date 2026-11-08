package com.orbiafly.api.modules.installmentexpense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@Import(AuthenticatedUserTestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class InstallmentExpenseApiTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanFinancialData() {
    jdbcTemplate.update("DELETE FROM installment_expense_installment");
    jdbcTemplate.update("DELETE FROM installment_purchase");
    jdbcTemplate.update("DELETE FROM loan_entry");
    jdbcTemplate.update("DELETE FROM loan_installment");
    jdbcTemplate.update("DELETE FROM loan");
    jdbcTemplate.update("DELETE FROM account_transfer");
    jdbcTemplate.update("DELETE FROM financial_transaction");
    jdbcTemplate.update("DELETE FROM financial_account");
  }

  @Test
  void createsInstallmentsDistributesCentsAndPaysOneInstallment() throws Exception {
    JsonNode account = performJson(post("/api/v1/accounts"), Map.of(
        "name", "Carteira",
        "type", "CASH",
        "institution", "",
        "openingBalance", 100.00), 201);

    JsonNode purchase = performJson(post("/api/v1/installment-expenses"), Map.of(
        "description", "Notebook",
        "totalAmount", 100.00,
        "installmentCount", 3,
        "firstInstallmentDate", "2026-09-10"), 201);

    assertThat(purchase.at("/installments/0/amount").decimalValue()).isEqualByComparingTo("33.34");
    assertThat(purchase.at("/installments/1/amount").decimalValue()).isEqualByComparingTo("33.33");
    assertThat(purchase.at("/installments/2/dueDate").asText()).isEqualTo("2026-11-10");

    String installmentId = purchase.at("/installments/0/id").asText();
    performJson(post("/api/v1/installment-expenses/installments/{id}/payment", installmentId), Map.of(
        "financialAccountId", account.get("id").asText(),
        "paymentDate", "2026-09-09"), 200);

    mockMvc.perform(get("/api/v1/installment-expenses"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].installments[0].status").value("PAID"))
        .andExpect(jsonPath("$[0].pendingAmount").value(66.66));
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
