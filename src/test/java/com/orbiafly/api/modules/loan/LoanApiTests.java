package com.orbiafly.api.modules.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbiafly.api.AuthenticatedUserTestConfiguration;
import java.math.BigDecimal;
import java.util.HashMap;
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
class LoanApiTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanLoans() {
    jdbcTemplate.update("DELETE FROM loan_entry");
    jdbcTemplate.update("DELETE FROM loan_installment");
    jdbcTemplate.update("DELETE FROM loan");
    jdbcTemplate.update("DELETE FROM financial_transaction");
    jdbcTemplate.update("DELETE FROM financial_account");
  }

  @Test
  void createsInstallmentLoanAndRegistersBorrowedPaymentsWithAccountDebit() throws Exception {
    JsonNode account = createAccount("Conta Caixa", "CHECKING", "Caixa", "5000.00");
    JsonNode loan = createLoan(Map.of(
        "name", "Empréstimo Nubank",
        "direction", "BORROWED",
        "counterparty", "Nubank",
        "totalAmount", "1000.00",
        "startDate", "2026-08-31",
        "installmentCount", 3,
        "installmentAmount", "333.33",
        "firstDueDate", "2026-09-10"));

    assertThat(loan.get("installments")).hasSize(3);
    assertThat(loan.at("/installments/0/amount").decimalValue()).isEqualByComparingTo("333.33");
    assertThat(loan.at("/installments/2/amount").decimalValue()).isEqualByComparingTo("333.34");
    assertThat(loan.at("/estimatedEndDate").asText()).isEqualTo("2026-11-10");

    JsonNode firstPayment = registerEntry(loan.get("id").asText(), account.get("id").asText(), null);
    assertThat(firstPayment.at("/remainingAmount").decimalValue()).isEqualByComparingTo("666.67");
    assertThat(firstPayment.at("/completedInstallmentCount").asInt()).isEqualTo(1);
    assertThat(firstPayment.at("/nextInstallment/installmentNumber").asInt()).isEqualTo(2);
    assertThat(firstPayment.at("/entries/0/financialAccountName").asText()).isEqualTo("Conta Caixa");

    JsonNode accountAfterPayment = getJson("/api/v1/accounts/" + account.get("id").asText());
    assertThat(accountAfterPayment.at("/currentBalance").decimalValue()).isEqualByComparingTo("4666.67");
    Integer debitCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM financial_transaction WHERE transaction_type = 'DEBIT' AND reference_type = 'LOAN_ENTRY'",
        Integer.class);
    assertThat(debitCount).isEqualTo(1);
  }

  @Test
  void registersPartialLentReceiptsAndCompletesLoanWithAccountCredits() throws Exception {
    JsonNode account = createAccount("Conta Nubank", "DIGITAL_ACCOUNT", "Nubank", "100.00");
    JsonNode loan = createLoan(Map.of(
        "name", "Empréstimo João",
        "direction", "LENT",
        "counterparty", "João",
        "totalAmount", "500.00",
        "startDate", "2026-08-31"));

    JsonNode firstReceipt = registerEntry(loan.get("id").asText(), account.get("id").asText(), "200.00");
    assertThat(firstReceipt.at("/status").asText()).isEqualTo("ACTIVE");
    assertThat(firstReceipt.at("/remainingAmount").decimalValue()).isEqualByComparingTo("300.00");
    assertThat(firstReceipt.at("/progressPercentage").decimalValue()).isEqualByComparingTo("40.00");
    assertThat(firstReceipt.at("/estimatedEndDate").isNull()).isTrue();

    JsonNode completion = registerEntry(loan.get("id").asText(), account.get("id").asText(), "300.00");
    assertThat(completion.at("/status").asText()).isEqualTo("COMPLETED");
    assertThat(completion.at("/remainingAmount").decimalValue()).isEqualByComparingTo("0.00");

    JsonNode overview = getJson("/api/v1/loans");
    assertThat(overview.at("/totalLentRemaining").decimalValue()).isEqualByComparingTo("0.00");
    assertThat(overview.at("/activeLoanCount").asInt()).isZero();

    JsonNode accountAfterReceipts = getJson("/api/v1/accounts/" + account.get("id").asText());
    assertThat(accountAfterReceipts.at("/currentBalance").decimalValue()).isEqualByComparingTo("600.00");
    Integer creditCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM financial_transaction WHERE transaction_type = 'CREDIT' AND reference_type = 'LOAN_ENTRY'",
        Integer.class);
    assertThat(creditCount).isEqualTo(2);
  }

  @Test
  void rejectsPartialAmountForScheduledLoanAndInactiveAccount() throws Exception {
    JsonNode account = createAccount("Carteira", "CASH", "", "1000.00");
    JsonNode loan = createLoan(Map.of(
        "name", "Banco",
        "direction", "BORROWED",
        "counterparty", "Banco",
        "totalAmount", "600.00",
        "startDate", "2026-08-31",
        "installmentCount", 2,
        "installmentAmount", "300.00",
        "firstDueDate", "2026-09-10"));

    mockMvc.perform(post("/api/v1/loans/{id}/entries", loan.get("id").asText())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "financialAccountId", account.get("id").asText(),
                "amount", "100.00",
                "occurredOn", "2026-09-10"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.entries[0].amount").value(300.00));
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

  private JsonNode createLoan(Map<String, Object> payload) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(payload)))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private JsonNode registerEntry(String loanId, String accountId, String amount) throws Exception {
    Map<String, Object> payload = new HashMap<>();
    payload.put("financialAccountId", accountId);
    if (amount != null) {
      payload.put("amount", amount);
    }
    payload.put("occurredOn", "2026-09-10");
    MvcResult result = mockMvc.perform(post("/api/v1/loans/{id}/entries", loanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(payload)))
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

  private String json(Object value) throws Exception {
    return objectMapper.writeValueAsString(value);
  }
}
