package dio.todolist.service;

import dio.todolist.domain.User;
import dio.todolist.handler.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long expirationSign;

    private SecretKey jwtKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    private Date expirationDate() {
        return new Date(System.currentTimeMillis() + expirationSign);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(expirationDate())
                .claim("UserId", user.getId())
                .signWith(jwtKey())
                .compact();
    }

    public boolean validateToken(String token, User user) {
        try {
            return user.getEmail().equals(extractUsername(token)) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Token inválido ou expirado");
        }
    }


}
