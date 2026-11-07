package com.cput.laundryecommercebookingsystem.security;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/*JwtService.java
 * Creates and checks login tokens (JWT, signed with HS256 and the JWT_SECRET).
 * A token says which student it belongs to and expires after app.jwt.expiration-ms.
 */
@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(secret.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT_SECRET must be base64. Generate one with: openssl rand -base64 48");
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET is too short. Generate one with: openssl rand -base64 48");
        }
        SecretKey key = new SecretKeySpec(bytes, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        this.expirationMs = expirationMs;
    }

    public String generateToken(Student student) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(student.getStudentId()))
                .claim("role", student.getRole().name())
                .issuedAt(now)
                .expiresAt(now.plusMillis(expirationMs))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /** The student ID in a valid, unexpired token; empty for anything forged, changed or expired. */
    public Optional<Long> parseStudentId(String token) {
        try {
            return Optional.of(Long.valueOf(decoder.decode(token).getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty(); // the token itself is never logged
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
