package com.codemaniac.jobtrackrai.dto.billing;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PlanResponse {
  String code;
  String name;
  long priceAmount;
  String currency;
  String billingInterval;
  List<String> features;
}
