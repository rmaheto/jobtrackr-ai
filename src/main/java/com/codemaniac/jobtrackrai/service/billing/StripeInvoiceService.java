package com.codemaniac.jobtrackrai.service.billing;

import com.codemaniac.jobtrackrai.dto.billing.InvoiceSummary;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceCollection;
import com.stripe.param.InvoiceListParams;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StripeInvoiceService {

  public List<InvoiceSummary> getRecentInvoices(final String stripeCustomerId, final int limit)
      throws StripeException {

    final InvoiceListParams params =
        InvoiceListParams.builder().setCustomer(stripeCustomerId).setLimit((long) limit).build();

    final InvoiceCollection invoices = Invoice.list(params);

    return invoices.getData().stream().map(this::toSummary).toList();
  }

  private InvoiceSummary toSummary(final Invoice invoice) {
    return InvoiceSummary.builder()
        .invoiceId(invoice.getId())
        .amountPaid(invoice.getAmountPaid())
        .currency(invoice.getCurrency())
        .createdAt(Instant.ofEpochSecond(invoice.getCreated()))
        .status(invoice.getStatus())
        .hostedInvoiceUrl(invoice.getHostedInvoiceUrl())
        .pdfUrl(invoice.getInvoicePdf())
        .build();
  }
}
