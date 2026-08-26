package com.orbiafly.api.modules.financialaccount.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountBalanceProjectionInterface {

  UUID getAccountId();

  BigDecimal getBalanceDelta();
}
