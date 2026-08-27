package com.orbiafly.api.modules.creditcard.infrastructure.persistence;

import com.orbiafly.api.modules.creditcard.domain.CreditCard;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditCardRepositoryInterface extends JpaRepository<CreditCard, UUID> {

  List<CreditCard> findAllByActiveTrueAndDeletedAtIsNullOrderByNameAsc();

  List<CreditCard> findAllByDeletedAtIsNullOrderByActiveDescNameAsc();

  Optional<CreditCard> findByIdAndDeletedAtIsNull(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT card FROM CreditCard card WHERE card.id = :id AND card.deletedAt IS NULL")
  Optional<CreditCard> findByIdForUpdate(@Param("id") UUID id);
}
