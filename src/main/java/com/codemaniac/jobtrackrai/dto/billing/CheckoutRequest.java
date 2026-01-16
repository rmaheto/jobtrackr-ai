package com.codemaniac.jobtrackrai.dto.billing;

import com.codemaniac.jobtrackrai.enums.PlanCode;

public record CheckoutRequest(PlanCode planCode) {}
