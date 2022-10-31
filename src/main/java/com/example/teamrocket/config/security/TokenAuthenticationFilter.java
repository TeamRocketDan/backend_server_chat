package com.example.teamrocket.config.security;

import com.example.teamrocket.config.jwt.AuthToken;
import com.example.teamrocket.config.jwt.AuthTokenProvider;
import com.example.teamrocket.utils.CommonRequestContext;
import com.example.teamrocket.utils.HeaderUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final AuthTokenProvider tokenProvider;
    private final CommonRequestContext commonRequestContext;
//    private final RedisTemplate redisTemplate;

    Set<String> urlSet = new HashSet<>(Arrays.asList(
            "/api/v1/auth/healthcheck",
            "/api/v1/auth/logout"
    ));

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)  throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String tokenStr = HeaderUtil.getAccessToken(request);
        AuthToken token = tokenProvider.convertAuthToken(tokenStr);

        if (!urlSet.contains(requestURI) && StringUtils.hasText(tokenStr)
            ) { // !requestURI.equals("/api/v1/auth/healthcheck")) {
//&& ObjectUtils.isEmpty(redisTemplate.opsForValue().get(tokenStr))
            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String uuid = token.getUuid();
            commonRequestContext.setMemberUuId(uuid);
        }

        filterChain.doFilter(request, response);
    }
}