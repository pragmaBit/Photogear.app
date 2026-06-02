package app.photogear.util;

import app.photogear.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final SecretKey KEY;
    private static final long      EXPIRY_MS;

    static {
        String secret = AppConfig.get("jwt.secret", "");
        if (secret.length() < 32) {
            throw new ExceptionInInitializerError(
                "jwt.secret debe tener al menos 32 caracteres. Configúralo en config.properties.");
        }
        KEY       = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        int hours = Integer.parseInt(AppConfig.get("jwt.expiry.hours", "24"));
        EXPIRY_MS = (long) hours * 3_600_000L;
    }

    public static String createToken(User user) {
        return Jwts.builder()
            .subject(user.getId())
            .claim("email",   user.getEmail())
            .claim("name",    user.getName())
            .claim("picture", user.getPicture())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRY_MS))
            .signWith(KEY)
            .compact();
    }

    public static Claims validateToken(String token) {
        return Jwts.parser()
            .verifyWith(KEY)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
