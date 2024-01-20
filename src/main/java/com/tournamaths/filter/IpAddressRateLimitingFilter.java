package com.tournamaths.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

public class IpAddressRateLimitingFilter implements Filter {
    /**
     * Filter to rate-limit by IP address;
     * 
     * NOTE this approach could disadvantage legitimate users behind a proxy/NAT.
     */

    // NOTE LoadingCache and AtomicInteger are thread-safe
    private LoadingCache<String, AtomicInteger> requestCountsRemainingPerIpAddress;

    public IpAddressRateLimitingFilter(int maxRequests){
        // We rate-limit to maxRequests requests per IP address per minute, per EC2 instance.
        // NOTE entries are evicted after 1-minute.
        requestCountsRemainingPerIpAddress = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(key -> new AtomicInteger(maxRequests));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Get client IP address or any other client identifier
        String clientIpAddress = request.getRemoteAddr();

        // Gets requests left in minute from cache for IP address, updating and returning maxRequests above if evicted.
        AtomicInteger requestsLeft = requestCountsRemainingPerIpAddress.get(clientIpAddress);

        if (requestsLeft.getAndDecrement() <= 0){
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return;
        }

        chain.doFilter(request, response);
    }
}
