package com.orbiafly.api.modules.financialaccount.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountBalanceProjection {

  UUID getAccountId();

  BigDecimal getBalanceDelta();
}
