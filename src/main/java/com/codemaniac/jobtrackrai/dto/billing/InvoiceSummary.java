package com.codemaniac.jobtrackrai.dto.billing;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceSummary {

  private String invoiceId;
  private Long amountPaid;
  private String currency;
  private Instant createdAt;
  private String status;
  private String hostedInvoiceUrl;
  private String pdfUrl;
}
