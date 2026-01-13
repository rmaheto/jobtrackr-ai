package com.codemaniac.jobtrackrai.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

  public StripeConfig(@Value("${stripe.apiKey}") final String apiKey) {
    Stripe.apiKey = apiKey;
  }
}
