package com.tournamaths.config;

import com.tournamaths.filter.CSPFilter;
import com.tournamaths.filter.IpAddressRateLimitingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  @Bean
  public IpAddressRateLimitingFilter loginAndRegistrationRateLimitingFilter() {
    return new IpAddressRateLimitingFilter(3, "/process_login", "/register");
  }

  @Bean
  public CSPFilter cspFilter() {
    return new CSPFilter();
  }
}
