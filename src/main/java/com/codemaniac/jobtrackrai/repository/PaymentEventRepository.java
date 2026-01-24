package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {

  boolean existsByProviderEventId(String providerEventId);
}
