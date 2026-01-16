package com.codemaniac.jobtrackrai.dto.brightdata;

import java.time.Instant;

public record ProviderSubscription(
    String subscriptionId,
    String customerId,
    String priceId,
    String status,
    Instant currentPeriodStart,
    Instant currentPeriodEnd,
    Boolean cancelAtPeriodEnd) {}
