package de.caritas.cob.liveservice.config.security;

import de.caritas.cob.liveservice.api.auth.AuthorisationService;
import de.caritas.cob.liveservice.api.auth.JwtAuthConverter;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

/**
 * Configuration class to provide the keycloak security configuration.
 */
@Configuration
@KeycloakConfiguration
public class WebSecurityConfig {

  private final AuthorisationService authorisationService;
  private final JwtAuthConverterProperties jwtAuthConverterProperties;

  @Autowired
  public WebSecurityConfig(AuthorisationService authorisationService, JwtAuthConverterProperties jwtAuthConverterProperties) {
    this.authorisationService = authorisationService;
    this.jwtAuthConverterProperties = jwtAuthConverterProperties;
  }

  protected static final String[] WHITE_LIST =
      new String[] {"/mails/docs", "/mails/docs/**", "/v2/api-docs", "/configuration/ui",
          "/swagger-resources/**", "/configuration/security", "/swagger-ui", "/swagger-ui/**", "/webjars/**"};

  @Bean
  SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(management -> management
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .sessionAuthenticationStrategy(sessionAuthenticationStrategy()))
        .authorizeHttpRequests(requests -> requests
            .requestMatchers(WHITE_LIST).permitAll()
            .requestMatchers(new NegatedRequestMatcher(new AntPathRequestMatcher("/live"))).permitAll()
            .requestMatchers(new NegatedRequestMatcher(new AntPathRequestMatcher("/live/**")))
            .permitAll());

    httpSecurity.oauth2ResourceServer(server -> server.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));
    return httpSecurity.build();
  }

  @Bean
  JwtAuthConverter jwtAuthConverter() {
    return new JwtAuthConverter(jwtAuthConverterProperties, authorisationService);
  }


  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new NullAuthenticatedSessionStrategy();
  }

  /**
   * Provides the keycloak configuration resolver bean.
   *
   * @return the configured {@link KeycloakConfigResolver}
   */
  @Bean
  KeycloakConfigResolver keycloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  @Bean
  @ConfigurationProperties(prefix = "keycloak", ignoreUnknownFields = false)
  KeycloakSpringBootProperties keycloakSpringBootProperties() {
    return new KeycloakSpringBootProperties();
  }

}
