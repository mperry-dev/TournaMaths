package com.tournamaths.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tournamaths.filter.IpAddressRateLimitingFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<IpAddressRateLimitingFilter> loginAndRegistrationRateLimitingFilter() {
        FilterRegistrationBean<IpAddressRateLimitingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new IpAddressRateLimitingFilter(3));
        bean.addUrlPatterns("/process_login", "/register");
        return bean;
    }
}
