package com.secke.product_service.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private static final String SECRET_KEY = "iNakY36pTMhFGgQhbzNBfX/IRPWMQ0z9EAIryh5CCDMm";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object roleClaim = claims.get("role");
        if (roleClaim instanceof List) {
            return ((List<?>) roleClaim).stream()
                    .map(roleObj -> ((Map<String, String>) roleObj).get("authority"))
                    .toList();
        }
        return List.of();
    }


    public boolean isTokenValid(String token) {
        final String username = extractUsername(token);
        return (username != null) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}
