package com.materialmaker.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Component
public class UserContext {
    private static final String HEADER = "X-Demo-User-Id";

    public String getUserId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "demo-anonymous";
        }
        HttpServletRequest request = attrs.getRequest();
        String userId = request.getHeader(HEADER);
        if (userId == null || userId.isBlank()) {
            // Fallback for demo convenience
            userId = "demo-default";
        }
        return userId;
    }

    public String ensureUserId() {
        return getUserId();
    }
}
