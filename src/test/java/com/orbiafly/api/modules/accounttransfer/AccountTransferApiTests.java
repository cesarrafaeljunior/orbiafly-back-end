package com.orbiafly.api.modules.accounttransfer;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
class AccountTransferApiTests {

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
  void createsTransferWithTwoLinkedTransactionsAndKeepsTotalBalance() throws Exception {
    String nubankId = createAccount("Nubank", "DIGITAL_ACCOUNT", "Nubank", "2000.00").get("id").asText();
    String interId = createAccount("Inter", "CHECKING", "Inter", "800.00").get("id").asText();

    JsonNode transfer = performJson(
        post("/api/v1/account-transfers"),
        Map.of(
            "sourceAccountId", nubankId,
            "destinationAccountId", interId,
            "amount", new BigDecimal("500.00"),
            "transferDate", "2026-09-06",
            "description", "Transferência para conta Inter"),
        201);

    String transferId = transfer.at("/transfer/id").asText();
    String sourceTransactionId = transfer.at("/transfer/sourceTransactionId").asText();
    String destinationTransactionId = transfer.at("/transfer/destinationTransactionId").asText();
    Assertions.assertThat(sourceTransactionId).isNotEqualTo(destinationTransactionId);
    Assertions.assertThat(transfer.at("/source/transaction/type").asText()).isEqualTo("TRANSFER_OUT");
    Assertions.assertThat(transfer.at("/destination/transaction/type").asText()).isEqualTo("TRANSFER_IN");
    Assertions.assertThat(transfer.at("/source/transaction/referenceId").asText()).isEqualTo(transferId);
    Assertions.assertThat(transfer.at("/destination/transaction/referenceId").asText()).isEqualTo(transferId);
    Assertions.assertThat(transfer.at("/source/financialAccount/currentBalance").decimalValue())
        .isEqualByComparingTo("1500.00");
    Assertions.assertThat(transfer.at("/destination/financialAccount/currentBalance").decimalValue())
        .isEqualByComparingTo("1300.00");
    Assertions.assertThat(accounts().at("/totalAvailableBalance").decimalValue())
        .isEqualByComparingTo("2800.00");

    mockMvc.perform(get("/api/v1/accounts/{id}/transactions", nubankId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].type").value("TRANSFER_OUT"))
        .andExpect(jsonPath("$[0].referenceType").value("ACCOUNT_TRANSFER"));
    mockMvc.perform(get("/api/v1/accounts/{id}/transactions", interId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].type").value("TRANSFER_IN"))
        .andExpect(jsonPath("$[0].referenceType").value("ACCOUNT_TRANSFER"));
  }

  @Test
  void rejectsSameAccountInvalidAmountInactiveAndAnotherUsersAccount() throws Exception {
    String nubankId = createAccount("Nubank", "DIGITAL_ACCOUNT", "Nubank", "2000.00").get("id").asText();
    String interId = createAccount("Inter", "CHECKING", "Inter", "800.00").get("id").asText();

    mockMvc.perform(post("/api/v1/account-transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "sourceAccountId", nubankId,
                "destinationAccountId", nubankId,
                "amount", new BigDecimal("500.00"),
                "transferDate", "2026-09-06"))))
        .andExpect(status().isBadRequest());

    mockMvc.perform(post("/api/v1/account-transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "sourceAccountId", nubankId,
                "destinationAccountId", interId,
                "amount", BigDecimal.ZERO,
                "transferDate", "2026-09-06"))))
        .andExpect(status().isBadRequest());

    mockMvc.perform(patch("/api/v1/accounts/{id}/deactivate", interId)).andExpect(status().isOk());
    mockMvc.perform(post("/api/v1/account-transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "sourceAccountId", nubankId,
                "destinationAccountId", interId,
                "amount", new BigDecimal("500.00"),
                "transferDate", "2026-09-06"))))
        .andExpect(status().isConflict());

    insertOtherUserAccount();
    mockMvc.perform(post("/api/v1/account-transfers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "sourceAccountId", nubankId,
                "destinationAccountId", "00000000-0000-0000-0000-000000000201",
                "amount", new BigDecimal("500.00"),
                "transferDate", "2026-09-06"))))
        .andExpect(status().isNotFound());

    Integer transferCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM account_transfer", Integer.class);
    Integer transactionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM financial_transaction", Integer.class);
    Assertions.assertThat(transferCount).isZero();
    Assertions.assertThat(transactionCount).isZero();
  }

  private JsonNode createAccount(String name, String type, String institution, String openingBalance)
      throws Exception {
    return performJson(post("/api/v1/accounts"), Map.of(
        "name", name,
        "type", type,
        "institution", institution,
        "openingBalance", new BigDecimal(openingBalance)), 201);
  }

  private JsonNode accounts() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/v1/accounts"))
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
        VALUES (?, 'transfer.other@example.com', 'transfer_other', 'x', 'x', 'ROLE_USER', TRUE)
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
