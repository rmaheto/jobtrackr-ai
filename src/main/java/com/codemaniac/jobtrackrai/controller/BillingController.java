package com.codemaniac.jobtrackrai.controller;

import com.codemaniac.jobtrackrai.dto.billing.BillingSummaryResponse;
import com.codemaniac.jobtrackrai.dto.billing.CheckoutRequest;
import com.codemaniac.jobtrackrai.dto.billing.InvoiceSummary;
import com.codemaniac.jobtrackrai.dto.billing.PlanResponse;
import com.codemaniac.jobtrackrai.dto.brightdata.PortalSessionResponse;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.model.ApiResponse;
import com.codemaniac.jobtrackrai.service.CurrentUserService;
import com.codemaniac.jobtrackrai.service.billing.BillingService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/billing")
public class BillingController {

  private final BillingService billingService;
  private final CurrentUserService currentUserService;

  @GetMapping("/plans")
  public ResponseEntity<ApiResponse<List<PlanResponse>>> getPlans() {

    return ResponseEntity.ok(
        ApiResponse.of("OK", "Available plans", billingService.getAvailablePlans()));
  }

  @PostMapping("/checkout")
  public ResponseEntity<ApiResponse<Map<String, String>>> createCheckout(
      @RequestBody final CheckoutRequest request) {

    final User user = currentUserService.getCurrentUser();

    final String checkoutUrl = billingService.createCheckoutSession(user, request.planCode());

    return ResponseEntity.ok(
        ApiResponse.of("OK", "Checkout session created", Map.of("checkoutUrl", checkoutUrl)));
  }

  @PostMapping("/cancel")
  public ResponseEntity<ApiResponse<Void>> cancelSubscription() {

    final User user = currentUserService.getCurrentUser();

    billingService.requestCancellation(user.getId());

    return ResponseEntity.ok(ApiResponse.of("OK", "Subscription cancellation requested", null));
  }

  @PostMapping("/portal")
  public ResponseEntity<ApiResponse<PortalSessionResponse>> openBillingPortal() {

    final User user = currentUserService.getCurrentUser();

    final String portalUrl = billingService.createBillingPortalUrl(user);

    return ResponseEntity.ok(
        ApiResponse.of(
            "OK", "Billing portal session created", new PortalSessionResponse(portalUrl)));
  }

  @GetMapping("/summary")
  public ResponseEntity<ApiResponse<BillingSummaryResponse>> getBillingSummary() {

    final User user = currentUserService.getCurrentUser();

    final BillingSummaryResponse summary = billingService.getSummary(user.getId());

    return ResponseEntity.ok(ApiResponse.of("OK", "Billing summary retrieved", summary));
  }

  @GetMapping("/invoices")
  public ResponseEntity<ApiResponse<List<InvoiceSummary>>> getInvoices() {

    final User user = currentUserService.getCurrentUser();

    final List<InvoiceSummary> invoices = billingService.getInvoiceHistory(user.getId());

    return ResponseEntity.ok(ApiResponse.of("OK", "Invoices retrieved", invoices));
  }
}
