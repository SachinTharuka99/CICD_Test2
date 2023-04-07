/**
 * Author : lahiru_p
 * Date : 2/22/2023
 * Time : 12:10 AM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.filter;


import com.epic.cms.config.JWTUtil;
import com.epic.cms.exception.JwtTokenMalformedException;
import com.epic.cms.exception.JwtTokenMissingException;
import com.epic.cms.model.ClientBean;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

//@Component
public class JwtAuthFilter implements GatewayFilter {
    @Autowired
    JWTUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = "";
        ClientBean clientBean = new ClientBean();
        ServerHttpRequest request = exchange.getRequest();
        final List<String> apiEndpoints = List.of("/register", "/login");
        Predicate<ServerHttpRequest> isApiSecured = r -> apiEndpoints.stream()
                .noneMatch(uri -> r.getURI().getPath().contains(uri));

        if (isApiSecured.test(request)) {
            if (!request.getHeaders().containsKey("Authorization")) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            final String token = request.getHeaders().getOrEmpty("Authorization").get(0);
            String jwt = "";
            try {
                jwt = jwtUtil.parseJwt(token);
                if (jwt != null) {
                    try {
                        jwtUtil.validateToken(token);
                    } catch (JwtTokenMalformedException | JwtTokenMissingException e) {
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.BAD_REQUEST);
                        return response.setComplete();
                    }
                    Claims claims = jwtUtil.getClaimsFromJwtToken(jwt);
                    if (claims != null) {
                        ip = (String) claims.get("client_ip");
                        String userRole = (String) claims.get("userrole");
                        String username = (String) claims.get("username");
                        int userlevel = (int) claims.get("userlevel");

                        clientBean.setUserrole(userRole);
                        clientBean.setUsername(username);
                        clientBean.setUserlevel(userlevel);

                        String client_ip = Objects.requireNonNull(request.getHeaders().get("x-real-ip")).get(0);
                        if(client_ip != null) {
                            if (!ip.equalsIgnoreCase(client_ip)) {
                                throw new AccessDeniedException("Access Denied");
                            } else {
                                exchange.getRequest().mutate().header("id", String.valueOf(claims.get("id"))).build();
                            }
                        }
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else {
                    throw new AccessDeniedException("Access Denied");
                }
            }catch (Exception e){
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                return response.setComplete();
            }
        }
        return chain.filter(exchange);
    }
}
