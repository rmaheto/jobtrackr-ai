package com.codemaniac.jobtrackrai.dto.billing;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentMethodService {

  public PaymentMethodSummary getDefaultPaymentMethod(final String stripeCustomerId)
      throws StripeException {

    final Customer customer = Customer.retrieve(stripeCustomerId);

    final String paymentMethodId =
        customer.getInvoiceSettings() != null
            ? customer.getInvoiceSettings().getDefaultPaymentMethod()
            : null;

    if (paymentMethodId == null) {
      return null; // User has no card on file
    }

    final PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);

    if (!"card".equals(pm.getType())) {
      return null;
    }

    final PaymentMethod.Card card = pm.getCard();

    return PaymentMethodSummary.builder()
        .brand(card.getBrand())
        .last4(card.getLast4())
        .expMonth(card.getExpMonth())
        .expYear(card.getExpYear())
        .build();
  }
}
