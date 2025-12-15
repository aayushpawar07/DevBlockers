package com.devblocker.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {
    
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    
    @Value("${jwt.access-token.expiration:900000}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration:86400000}")
    private long refreshTokenExpiration;
    
    public JwtService(
            @Value("${jwt.private-key.path:classpath:jwt/private-key.pem}") String privateKeyPath,
            @Value("${jwt.public-key.path:classpath:jwt/public-key.pem}") String publicKeyPath,
            ResourceLoader resourceLoader) {
        RSAPrivateKey privKey;
        RSAPublicKey pubKey;
        try {
            privKey = loadPrivateKey(privateKeyPath, resourceLoader);
            pubKey = loadPublicKey(publicKeyPath, resourceLoader);
        } catch (Exception e) {
            log.error("Failed to load RSA keys, generating new ones", e);
            // Generate keys if loading fails
            KeyPair keyPair = generateKeyPair();
            privKey = (RSAPrivateKey) keyPair.getPrivate();
            pubKey = (RSAPublicKey) keyPair.getPublic();
            log.warn("Using in-memory keys. For production, provide RSA keys via configuration.");
        }
        this.privateKey = privKey;
        this.publicKey = pubKey;
    }
    
    private RSAPrivateKey loadPrivateKey(String keyPath, ResourceLoader resourceLoader) 
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String keyContent = loadKeyContent(keyPath, resourceLoader);
        keyContent = keyContent.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(spec);
    }
    
    private RSAPublicKey loadPublicKey(String keyPath, ResourceLoader resourceLoader) 
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String keyContent = loadKeyContent(keyPath, resourceLoader);
        keyContent = keyContent.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(spec);
    }
    
    private String loadKeyContent(String keyPath, ResourceLoader resourceLoader) throws IOException {
        Resource resource = resourceLoader.getResource(keyPath);
        if (resource.exists()) {
            if (keyPath.startsWith("classpath:")) {
                return new String(resource.getInputStream().readAllBytes());
            } else {
                return Files.readString(Paths.get(keyPath));
            }
        } else {
            throw new IOException("Key file not found: " + keyPath);
        }
    }
    
    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }
    
    public String generateAccessToken(UUID userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("email", email);
        claims.put("role", role);
        claims.put("type", "access");
        
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(privateKey)
                .compact();
    }
    
    public String generateRefreshToken(UUID userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("email", email);
        claims.put("type", "refresh");
        
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(privateKey)
                .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }
    
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
}

