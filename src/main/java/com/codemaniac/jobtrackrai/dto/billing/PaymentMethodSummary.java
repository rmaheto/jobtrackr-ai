package com.codemaniac.jobtrackrai.dto.billing;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentMethodSummary {
  private String brand;
  private String last4;
  private Long expMonth;
  private Long expYear;
}
