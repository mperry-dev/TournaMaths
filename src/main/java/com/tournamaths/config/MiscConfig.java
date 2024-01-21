package com.tournamaths.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableRedisHttpSession
@EnableTransactionManagement // Enable Spring's annotation-driven transaction management capability,
                             // across all environments
public class MiscConfig {
  // This is for miscellaneous configurations - particularly where the annotations above have to be
  // added to a configuration class, but there wasn't a need for a full configuration class.
}
