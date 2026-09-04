package com.orbiafly.api.infrastructure.security;

import com.orbiafly.api.modules.user.domain.User;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenServiceImpl implements JwtTokenServiceInterface {

  private final JwtEncoder jwtEncoder;
  private final SecurityProperties securityProperties;

  public JwtTokenServiceImpl(JwtEncoder jwtEncoder, SecurityProperties securityProperties) {
    this.jwtEncoder = jwtEncoder;
    this.securityProperties = securityProperties;
  }

  @Override
  public String createTokenForUser(User user) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(getExpirationSeconds());
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(securityProperties.jwt().issuer())
        .issuedAt(now)
        .expiresAt(expiresAt)
        .subject(user.getId().toString())
        .claim("username", user.getUsername())
        .claim("email", user.getEmail())
        .claim("role", user.getRole().name())
        .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  @Override
  public long getExpirationSeconds() {
    return securityProperties.jwt().expirationMinutes() * 60;
  }
}
