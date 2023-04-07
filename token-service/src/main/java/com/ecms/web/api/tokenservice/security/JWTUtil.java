package com.ecms.web.api.tokenservice.security;

import com.ecms.web.api.tokenservice.model.bean.SystemuserBean;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtil {

    @Autowired
    private Environment env;

    private final String HEADER = "Authorization";
    private final String PREFIX = "Bearer ";

    public String generateToken(String client_ip, SystemuserBean systemuserBean) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("client_ip", client_ip);
        claims.put("username", systemuserBean.getUsername());
        claims.put("fullname", systemuserBean.getFullname());
        claims.put("userlevel", systemuserBean.getUserlevel());
        claims.put("userrole", systemuserBean.getUserrole());
        claims.put("pwdStatus", systemuserBean.getPasswordstatus());

        String secretKey = env.getProperty("jwt.secret");
        String tokenIssuerName = env.getProperty("jwt.issuer");
        long token_validity = Long.parseLong(env.getProperty("jwt.token-validity"));

        String token = Jwts
                .builder()
                .setClaims(claims)
                .setIssuer(tokenIssuerName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + token_validity * 1000))
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes()).compact();

        return "Bearer " + token;
    }

    public String parseJwt(String headerAuth) {
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(PREFIX))
            return headerAuth.substring(7, headerAuth.length());
        return null;
    }

    public Claims getClaimsFromJwtToken(String token) {
        String secretKey = env.getProperty("jwt.secret");
        return Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token).getBody();
    }


}
