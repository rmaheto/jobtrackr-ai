package com.codemaniac.jobtrackrai.dto.brightdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
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

  public String format() {
    if (minAmount == null && maxAmount == null) {
      return null;
    }

    final NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
    formatter.setCurrency(Currency.getInstance("USD"));

    final String min = minAmount != null ? formatter.format(minAmount) : "";

    final String max = maxAmount != null ? formatter.format(maxAmount) : "";

    final String period = paymentPeriod != null ? paymentPeriod : "";

    if (!min.isEmpty() && !max.isEmpty()) {
      return String.format("%s – %s /%s", min, max, period);
    }

    return String.format("%s /%s", !min.isEmpty() ? min : max, period);
  }
}
