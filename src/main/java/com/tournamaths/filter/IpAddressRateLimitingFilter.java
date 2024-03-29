package com.tournamaths.filter;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;

// TODO = re-implement this for IP Address rate limiting using Redis (better to be more consistent,
// and this could have variable rates at which limiting occurs as it depends which EC2 instance
// served by)
public class IpAddressRateLimitingFilter implements Filter {
  /**
   * Filter to rate-limit by IP address. This allows setting whatever limit we like - Web ACLs only
   * allow minimum limits of 100 per 5 minutes (I'd like login/registration to have lower limits).
   *
   * <p>NOTE this approach could disadvantage legitimate users behind a proxy/NAT.
   *
   * <p>The purpose of this is to protect the EC2 instances from being overloaded, rather than
   * numerically-consistent per-account security (e.g. preventing too many login attempts per minute
   * per account being subjected to it). For per-account security, should implement a centralized
   * stored cache (such as Redis) and cache by user id.
   */

  // NOTE LoadingCache and AtomicInteger are thread-safe
  private LoadingCache<String, AtomicInteger> requestCountsRemainingPerIpAddress;

  private String[] endpoints;

  public IpAddressRateLimitingFilter(int maxRequests, String... endpoints) {
    // We rate-limit to maxRequests requests per IP address per minute, per EC2 instance.
    // NOTE entries are evicted after 1-minute.
    requestCountsRemainingPerIpAddress =
        Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(key -> new AtomicInteger(maxRequests));
    this.endpoints = endpoints;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    // Get client IP address or any other client identifier
    String clientIpAddress = request.getRemoteAddr();

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String requestUri = httpRequest.getRequestURI();

    // NOTE - the request URI doesn't include query parameters, so using .equals here is correct
    if (Arrays.stream(endpoints).anyMatch(endpoint -> requestUri.equals(endpoint))) {
      // Gets requests left in minute from cache for IP address, updating and returning maxRequests
      // above if evicted.
      AtomicInteger requestsLeft = requestCountsRemainingPerIpAddress.get(clientIpAddress);

      if (requestsLeft.getAndDecrement() <= 0) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        return;
      }
    }

    chain.doFilter(request, response);
  }
}
