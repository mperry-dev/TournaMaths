package com.tournamaths.config;

import com.tournamaths.filter.IpAddressRateLimitingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  @Bean
  public IpAddressRateLimitingFilter loginAndRegistrationRateLimitingFilter() {
    return new IpAddressRateLimitingFilter(3, "/process_login", "/register");
  }
}
