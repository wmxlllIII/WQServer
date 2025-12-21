package com.example.test.server.interceptor;


import com.example.test.common.constant.JwtClaimsConstant;
import com.example.test.common.context.BaseContext;
import com.example.test.common.properties.JwtProperties;
import com.example.test.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    private final Map<String, Object> claims = new HashMap<>();
    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getUserTokenName());

        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            long uuNumber = Long.parseLong(claims.get(JwtClaimsConstant.EMP_ID).toString());
            log.info("当前userId：{}", uuNumber);

            claims.put(JwtClaimsConstant.EMP_ID, uuNumber);
            String newToken = JwtUtil.createJWT(
                    jwtProperties.getUserSecretKey(),
                    jwtProperties.getUserTtl(),
                    claims
            );

            BaseContext.setCurrentId(uuNumber);
            response.setHeader(jwtProperties.getUserTokenName(), newToken);

            return true;
        } catch (Exception ex) {
            response.setStatus(401);
            log.info("Exception:{}", ex.getMessage());
            return false;
        }
    }
}
