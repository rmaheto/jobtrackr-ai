package com.codemaniac.jobtrackrai.dto.billing;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingSummaryResponse {

  private String planName;
  private String planCode;

  private Long priceAmount;
  private String currency;
  private String billingInterval;

  private String status;

  private Instant currentPeriodEnd;
  private Instant nextBillingDate;

  private PaymentMethodSummary paymentMethod;

  private List<String> features;

  private boolean canCancel;
  private boolean canUpgrade;
}
