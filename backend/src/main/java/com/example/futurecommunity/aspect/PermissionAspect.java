package com.example.futurecommunity.aspect;

import com.example.futurecommunity.annotation.RequirePermission;
import com.example.futurecommunity.exception.PermissionDeniedException;
import com.example.futurecommunity.util.PermissionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);

    private final PermissionUtil permissionUtil;

    public PermissionAspect(PermissionUtil permissionUtil) {
        this.permissionUtil = permissionUtil;
    }

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        String requiredPermission = requirePermission.value();
        String errorMessage = requirePermission.message();
        
        logger.debug("开始权限检查，所需权限: {}", requiredPermission);
        
        try {
            boolean hasPermission = permissionUtil.hasPermission(requiredPermission);
            
            if (!hasPermission) {
                logger.warn("权限检查失败，用户缺少权限: {}", requiredPermission);
                throw new PermissionDeniedException(errorMessage);
            }
            
            logger.debug("权限检查通过，权限: {}", requiredPermission);
            return joinPoint.proceed();
            
        } catch (PermissionDeniedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("权限检查过程中发生异常", e);
            throw e;
        }
    }
}