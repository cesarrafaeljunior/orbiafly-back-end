package com.orbiafly.api.infrastructure.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final ObjectMapper objectMapper;
  private final Clock clock;
  private final String secret;
  private final String issuer;
  private final String audience;
  private final long expirationMinutes;

  public JwtService(
      ObjectMapper objectMapper,
      Clock clock,
      @Value("${app.security.jwt-secret}") String secret,
      @Value("${app.security.jwt-issuer}") String issuer,
      @Value("${app.security.jwt-audience}") String audience,
      @Value("${app.security.jwt-expiration-minutes}") long expirationMinutes) {
    if (secret == null || secret.length() < 32) {
      throw new IllegalArgumentException("APP_JWT_SECRET deve ter ao menos 32 caracteres.");
    }
    this.objectMapper = objectMapper;
    this.clock = clock;
    this.secret = secret;
    this.issuer = issuer;
    this.audience = audience;
    this.expirationMinutes = expirationMinutes;
  }

  public String createToken(AppUserPrincipal principal) {
    Instant now = clock.instant();
    Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("iss", issuer);
    claims.put("aud", audience);
    claims.put("sub", principal.id().toString());
    claims.put("username", principal.getUsername());
    claims.put("email", principal.email());
    claims.put("roles", principal.roles().stream().map(Enum::name).toList());
    claims.put("ver", principal.tokenVersion());
    claims.put("iat", now.getEpochSecond());
    claims.put("exp", now.plusSeconds(expirationMinutes * 60).getEpochSecond());
    String unsigned = base64Json(header) + "." + base64Json(claims);
    return unsigned + "." + base64Url(hmac(unsigned));
  }

  public JwtClaims parseToken(String token) {
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw invalidToken();
    }
    Map<String, Object> header = readJson(parts[0]);
    if (!"HS256".equals(header.get("alg")) || !"JWT".equals(header.get("typ"))) {
      throw invalidToken();
    }
    String unsigned = parts[0] + "." + parts[1];
    if (!MessageDigestComparator.matches(base64Url(hmac(unsigned)), parts[2])) {
      throw invalidToken();
    }
    Map<String, Object> claims = readJson(parts[1]);
    long now = clock.instant().getEpochSecond();
    Number issuedAt = numberClaim(claims, "iat");
    Number expiration = numberClaim(claims, "exp");
    Number tokenVersion = numberClaim(claims, "ver");
    if (!issuer.equals(claims.get("iss"))
        || !audience.equals(claims.get("aud"))
        || issuedAt.longValue() > now + 30
        || expiration.longValue() <= now) {
      throw invalidToken();
    }
    try {
      return new JwtClaims(
          UUID.fromString((String) claims.get("sub")),
          (String) claims.get("username"),
          (String) claims.get("email"),
          ((List<?>) claims.get("roles")).stream().map(String::valueOf).toList(),
          tokenVersion.longValue());
    } catch (RuntimeException exception) {
      throw invalidToken();
    }
  }

  private Number numberClaim(Map<String, Object> claims, String name) {
    Object value = claims.get(name);
    if (value instanceof Number number) {
      return number;
    }
    throw invalidToken();
  }

  private String base64Json(Object value) {
    try {
      return base64Url(objectMapper.writeValueAsBytes(value));
    } catch (Exception exception) {
      throw new IllegalStateException("Não foi possível gerar token.");
    }
  }

  private Map<String, Object> readJson(String encoded) {
    try {
      return objectMapper.readValue(Base64.getUrlDecoder().decode(encoded), new TypeReference<>() {});
    } catch (Exception exception) {
      throw invalidToken();
    }
  }

  private byte[] hmac(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    } catch (Exception exception) {
      throw new IllegalStateException("Não foi possível assinar token.");
    }
  }

  private String base64Url(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private IllegalArgumentException invalidToken() {
    return new IllegalArgumentException("Token inválido.");
  }

  public record JwtClaims(
      UUID userId,
      String username,
      String email,
      List<String> roles,
      long tokenVersion) {
  }
}
