package com.codemaniac.jobtrackrai.dto.billing;

import com.codemaniac.jobtrackrai.dto.FeatureResponse;
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
  List<FeatureResponse> features;
}
