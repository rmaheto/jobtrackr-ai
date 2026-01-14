package com.codemaniac.jobtrackrai.dto.brightdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkedInSalary {

  @JsonProperty("min_amount")
  private Integer minAmount;

  @JsonProperty("max_amount")
  private Integer maxAmount;

  private String currency;

  @JsonProperty("payment_period")
  private String paymentPeriod;
}
