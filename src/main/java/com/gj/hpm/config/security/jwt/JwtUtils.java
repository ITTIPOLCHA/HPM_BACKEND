package com.gj.hpm.config.security.jwt;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.gj.hpm.config.security.services.UserDetailsImpl;
import com.gj.hpm.dto.response.JwtClaimsDTO;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class JwtUtils {
  @Value("${hpm.app.jwtSecret}")
  private String jwtSecret;

  @Value("${hpm.app.jwtExpirationMs}")
  private int jwtExpirationMs;

  public String generateJwtToken(Authentication authentication) {

    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

    String role = userPrincipal.getAuthorities().stream()
        .findFirst()
        .map(GrantedAuthority::getAuthority)
        .orElseThrow(() -> new RuntimeException("User has no roles assigned"));

    Date issuedAt = new Date();
    Date expirationAt = calculateExpirationDate(issuedAt);

    return Jwts.builder()
        .setSubject(userPrincipal.getEmail())
        .setId(userPrincipal.getId())
        .claim("role", role)
        .claim("name", userPrincipal.getName())
        .claim("lineId", userPrincipal.getLineId())
        .setIssuedAt(issuedAt)
        .setExpiration(expirationAt)
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  private Date calculateExpirationDate(Date issuedAt) {
    return new Date(issuedAt.getTime() + jwtExpirationMs);
  }

  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }

  public String getEmailFromJwtToken(String token) {
    try {
      return Jwts.parserBuilder().setSigningKey(key()).build()
          .parseClaimsJws(token).getBody().getSubject();
    } catch (Exception e) {
      log.error("Error validating role: " + e.getMessage());
    }
    return null;
  }

  public String getEmailFromHeader(String header) {
    try {
      String tokenWithoutBearer = header.substring("Bearer ".length());
      return Jwts.parserBuilder().setSigningKey(key()).build()
          .parseClaimsJws(tokenWithoutBearer).getBody().getSubject();
    } catch (Exception e) {
      log.error("Error validating role: " + e.getMessage());
    }
    return null;
  }

  public String getIdFromHeader(String header) {
    try {
      String tokenWithoutBearer = header.substring("Bearer ".length());
      return Jwts.parserBuilder().setSigningKey(key()).build()
          .parseClaimsJws(tokenWithoutBearer).getBody().getId();
    } catch (Exception e) {
      log.error("Error validating role: " + e.getMessage());
    }
    return null;
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
      return true;
    } catch (MalformedJwtException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
  }

  public JWTClaimsSet decodeES256Jwt(String jwt) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(jwt);
      return signedJWT.getJWTClaimsSet();
    } catch (Exception e) {
      log.error("Error decoding ES256 JWT: " + e.getMessage());
      return null;
    }
  }

  public JwtClaimsDTO decodeJwtClaimsDTO(String jwt) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(jwt);
      JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

      JwtClaimsDTO claimsDTO = new JwtClaimsDTO();
      claimsDTO.setSubject(claimsSet.getSubject());
      claimsDTO.setJwtId(claimsSet.getJWTID());
      claimsDTO.setRole(claimsSet.getStringClaim("role"));
      claimsDTO.setName(claimsSet.getStringClaim("name"));
      claimsDTO.setLineId(claimsSet.getStringClaim("lineId"));

      return claimsDTO;
    } catch (Exception e) {
      log.error("Error decoding JWT: " + e.getMessage());
      return null;
    }
  }

}
