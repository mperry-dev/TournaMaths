package com.tournamaths.util;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class NonceBean {
  /*
   * Proxied "singleton" bean containing a nonce which is recreated once-per-request, and can be autowired globally.
   * Using the proxy mode so that get a different object behind the proxy per-request (and thus a different nonce per-request).
   */
  private final String nonce;

  public NonceBean() {
    byte[] nonceBytes = new byte[20];
    new SecureRandom().nextBytes(nonceBytes);
    this.nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);
  }

  public String getNonce() {
    return nonce;
  }
}
