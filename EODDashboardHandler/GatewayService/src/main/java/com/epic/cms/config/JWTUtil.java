package com.epic.cms.config;

import com.epic.cms.exception.JwtTokenMalformedException;
import com.epic.cms.exception.JwtTokenMissingException;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JWTUtil {

    @Autowired
    private Environment env;

    private final String HEADER = "Authorization";
    private final String PREFIX = "Bearer ";


    public String parseJwt(String headerAuth) {
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(PREFIX))
            return headerAuth.substring(7, headerAuth.length());
        return null;
    }

    public Claims getClaimsFromJwtToken(String token) {
        String secretKey = "EPICCMSREVAMP";//env.getProperty("jwt.secret");
        return Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token).getBody();
    }

    public void validateToken(final String token) throws JwtTokenMalformedException, JwtTokenMissingException {
        String secretKey = "EPICCMSREVAMP";
        try {
            Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token);
        } catch (SignatureException ex) {
            throw new JwtTokenMalformedException("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            throw new JwtTokenMalformedException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            throw new JwtTokenMalformedException("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            throw new JwtTokenMalformedException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            throw new JwtTokenMissingException("JWT claims string is empty.");
        }
    }
}
