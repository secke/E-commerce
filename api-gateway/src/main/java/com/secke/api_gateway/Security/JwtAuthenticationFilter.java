package com.secke.api_gateway.Security;

import com.secke.api_gateway.Service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter implements WebFilter {
    private static final String SECRET_KEY = "iNakY36pTMhFGgQhbzNBfX/IRPWMQ0z9EAIryh5CCDMm";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        System.out.println("JwtAuthenticationFilter is invoked");
        ServerHttpRequest request = exchange.getRequest();
        List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);

        if (authHeaders == null || authHeaders.isEmpty() || !authHeaders.get(0).startsWith("Bearer ")) {
            return chain.filter(exchange); // Skip if no Authorization header
        }

        String jwt = authHeaders.get(0).substring(7); // Extract token

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            String username = jwtService.extractUsername(jwt);
            Object roleClaim = claims.get("role");
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            if (roleClaim instanceof List) {
                List<?> roleList = (List<?>) roleClaim;
                for (Object roleObject : roleList) {
                    if (roleObject instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> roleMap = (Map<String, String>) roleObject;
                        String authority = roleMap.get("authority");
                        if (authority != null) {
                            authorities.add(new SimpleGrantedAuthority(authority));
                        }
                    }
                }
            }
            if (jwtService.isTokenValid(jwt)) {
                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );
                System.out.println("Authentication created for user: " + username);
                ReactiveSecurityContextHolder.getContext()
                        .doOnNext(context -> System.out.println("Authentication: " + context.getAuthentication()))
                        .subscribe();

                // Store authentication in security context
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

            }
            return chain.filter(exchange);
        } catch (Exception e) {
            System.out.println("Invalid JWT: " + e.getMessage());
            return chain.filter(exchange); // Proceed without authentication
        }
    }
}
