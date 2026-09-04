package com.orbiafly.api.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CorsConfigurationSource corsConfigurationSource,
      SecurityProperties securityProperties) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    if (!securityProperties.enabled()) {
      http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
      return http.build();
    }

    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health", "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  JwtEncoder jwtEncoder(SecurityProperties securityProperties) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecret(securityProperties).getEncoded()));
  }

  @Bean
  JwtDecoder jwtDecoder(SecurityProperties securityProperties) {
    return NimbusJwtDecoder.withSecretKey(jwtSecret(securityProperties)).macAlgorithm(MacAlgorithm.HS256).build();
  }

  private SecretKeySpec jwtSecret(SecurityProperties securityProperties) {
    byte[] secret = securityProperties.jwt().secret().getBytes(StandardCharsets.UTF_8);
    if (secret.length < 32) {
      throw new BadCredentialsException("APP_JWT_SECRET precisa ter pelo menos 32 bytes.");
    }
    return new SecretKeySpec(secret, "HmacSHA256");
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> java.util.List.of(
        new SimpleGrantedAuthority(jwt.getClaimAsString("role"))));
    return converter;
  }
}
