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
  private final long expirationMinutes;

  public JwtService(
      ObjectMapper objectMapper,
      Clock clock,
      @Value("${app.security.jwt-secret}") String secret,
      @Value("${app.security.jwt-expiration-minutes}") long expirationMinutes) {
    this.objectMapper = objectMapper;
    this.clock = clock;
    this.secret = secret;
    this.expirationMinutes = expirationMinutes;
  }

  public String createToken(AppUserPrincipal principal) {
    Instant now = clock.instant();
    Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("sub", principal.id().toString());
    claims.put("username", principal.getUsername());
    claims.put("email", principal.email());
    claims.put("roles", principal.roles().stream().map(Enum::name).toList());
    claims.put("iat", now.getEpochSecond());
    claims.put("exp", now.plusSeconds(expirationMinutes * 60).getEpochSecond());
    String unsigned = base64Json(header) + "." + base64Json(claims);
    return unsigned + "." + base64Url(hmac(unsigned));
  }

  public JwtClaims parseToken(String token) {
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Token inválido.");
    }
    String unsigned = parts[0] + "." + parts[1];
    String expected = base64Url(hmac(unsigned));
    if (!MessageDigestComparator.matches(expected, parts[2])) {
      throw new IllegalArgumentException("Token inválido.");
    }
    Map<String, Object> claims = readJson(parts[1]);
    Number expiration = (Number) claims.get("exp");
    if (expiration == null || expiration.longValue() < clock.instant().getEpochSecond()) {
      throw new IllegalArgumentException("Token expirado.");
    }
    return new JwtClaims(
        UUID.fromString((String) claims.get("sub")),
        (String) claims.get("username"),
        (String) claims.get("email"),
        ((List<?>) claims.get("roles")).stream().map(String::valueOf).toList());
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
      byte[] json = Base64.getUrlDecoder().decode(encoded);
      return objectMapper.readValue(json, new TypeReference<>() {});
    } catch (Exception exception) {
      throw new IllegalArgumentException("Token inválido.");
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

  public record JwtClaims(UUID userId, String username, String email, List<String> roles) {
  }
}
