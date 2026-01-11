package com.codemaniac.jobtrackrai.interceptor;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class OutboundRequestInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(
      final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
      throws IOException {
    final HttpHeaders headers = request.getHeaders();

    generateRequestHeaders(headers);
    return execution.execute(request, body);
  }

  private void generateRequestHeaders(final HttpHeaders headers) {
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
  }
}
