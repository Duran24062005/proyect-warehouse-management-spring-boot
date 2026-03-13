package com.proyectS1.warehouse_management.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Service
public class JwtService {

    private final String SECRET_KEY = "";
    private final long EXPIRATION_TIME = 1000 * 60 * 30; // 30 Minutes

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(this.SECRET_KEY.getBytes());
    }

    public String generateToken(String username){
        return Jwts.builder()
            // Main information of token (who is the user)
            .subject(username)
            // Date in which the token is created
            .issuedAt(new Date())
            // Date on which the token expires
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            // Sign whith the algorithm HS256 using my secret key
            .signWith(getKey())
            // Building the final token in String format
            .compact();
    }

    public String validateToken(String token) {
        /*
         * Este método:
         * - Verifica que la firma sea correcta
         * - Verifica que no esté expirado
         * - Si todo está bien, devuelve el username
         *
         * Si algo falla, devuelve null.
         */
        try {
            return Jwts.parser()
                    // Le paso la misma clave con la que firmé
                    .verifyWith(getKey())
                    .build()

                    // Intenta parsear el token
                    .parseSignedClaims(token)

                    // Obtengo el body (claims)
                    .getPayload()

                    // Devuelvo el subject (username)
                    .getSubject();
        } catch (ExpiredJwtException e) {
            // El token ya no es válido por tiempo
            System.out.println("Token expirado: " + e.getMessage());
        } catch (SignatureException e) {
            // La firma no coincide (el token fue manipulado o la clave es distinta)
            System.out.println("Firma del token inválida");
        } catch (MalformedJwtException e) {
            // El formato del string no es un JWT válido (faltan puntos, etc.)
            System.out.println("Token mal formado");
        } catch (UnsupportedJwtException e) {
            // El token no es un JWS firmado
            System.out.println("Token no soportado");
        } catch (IllegalArgumentException e) {
            // El token está vacío o es nulo
            System.out.println("Claims del token vacíos");
        } catch (JwtException e) {
            // Cualquier otro error relacionado con JJWT
            System.out.println("Error general de JWT: " + e.getMessage());
        }
        /*
         * Si el token:
         * - Está vencido
         * - Fue modificado
         * - La firma no coincide
         *
         * Lanza excepción y retorna en null.
         */

        return null;
    }
    
}
