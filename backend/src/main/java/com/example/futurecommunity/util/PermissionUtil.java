package com.example.futurecommunity.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissionUtil {

    private final JwtUtil jwtUtil;

    public PermissionUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public List<String> getCurrentUserPermissions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() instanceof String) {
                String token = (String) authentication.getCredentials();
                return jwtUtil.getPermissionsFromToken(token);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasPermission(String permission) {
        List<String> permissions = getCurrentUserPermissions();
        return permissions != null && permissions.contains(permission);
    }

    public boolean hasAnyPermission(String... permissions) {
        List<String> userPermissions = getCurrentUserPermissions();
        if (userPermissions == null) {
            return false;
        }
        for (String permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllPermissions(String... permissions) {
        List<String> userPermissions = getCurrentUserPermissions();
        if (userPermissions == null) {
            return false;
        }
        for (String permission : permissions) {
            if (!userPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }
}