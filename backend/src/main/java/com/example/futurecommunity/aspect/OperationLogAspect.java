package com.example.futurecommunity.aspect;

import com.example.futurecommunity.model.OperationLog;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.OperationLogService;
import com.example.futurecommunity.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private UserService userService;

    // 定义切点
    @Pointcut("execution(* com..controller..*(..))")
    public void operationLogPointcut() {
    }

    // 使用 @Around 环绕通知实现
    @Around("operationLogPointcut()")
    public Object aroundOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 检查请求方法，仅记录非查询类接口操作
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            String httpMethod = request.getMethod();
            // 如果是 GET 请求，直接执行并返回，不记录日志
            if ("GET".equals(httpMethod)) {
                return joinPoint.proceed();
            }
        }

        OperationLog operationLog = new OperationLog();
        Object result = null;
        String status = "SUCCESS";

        try {
            // 1. 获取当前登录用户（从 SecurityContext 中获取）
            getCurrentUserInfo(operationLog);

            // 2. 获取用户角色
            // 已在 getCurrentUserInfo 中获取

            // 获取请求信息
            if (request != null) {
                // 获取请求方法：request.getMethod() + " " + request.getRequestURI()
                String method = request.getMethod() + " " + request.getRequestURI();
                operationLog.setMethod(method);

                // 获取请求IP：request.getRemoteAddr()
                String ip = request.getRemoteAddr();
                operationLog.setIp(ip);
            }

            // 获取操作动作（转换为中文描述）
            String action = getChineseActionName(joinPoint);
            operationLog.setAction(action);

            // 生成友好的操作详情（人+操作+对象+什么事情）
            String detail = generateFriendlyDetail(joinPoint, operationLog);
            operationLog.setDetail(detail);

            // 4. 执行目标方法并统计耗时
            long start = System.currentTimeMillis();
            result = joinPoint.proceed();
            long costTime = System.currentTimeMillis() - start;
            operationLog.setCostTime(costTime);

            // 5. 如果成功：status = "SUCCESS"
            status = "SUCCESS";

        } catch (Throwable throwable) {
            // 6. 如果异常：status = "FAIL"
            status = "FAIL";
            // 记录异常信息
            String detail = operationLog.getDetail();
            if (detail != null) {
                detail += "\n异常信息: " + throwable.getMessage();
            } else {
                detail = "异常信息: " + throwable.getMessage();
            }
            operationLog.setDetail(detail);
            // 重新抛出异常
            throw throwable;
        } finally {
            // 设置状态和创建时间
            operationLog.setStatus(status);
            operationLog.setCreatedAt(new Date());

            // 保存操作日志
            saveOperationLog(operationLog);
        }

        return result;
    }

    /**
     * 获取当前登录用户信息
     */
    private void getCurrentUserInfo(OperationLog operationLog) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    String username = userDetails.getUsername();
                    try {
                        // 从username中解析出userId
                        Long userId = Long.parseLong(username);
                        operationLog.setUserId(userId);
                    } catch (NumberFormatException e) {
                        logger.warn("解析userId失败: {}", username);
                    }

                    // 获取用户角色
                    String role = getUserRole(userDetails);
                    operationLog.setRole(role);
                }
            }
        } catch (Exception e) {
            logger.error("获取当前用户信息异常", e);
        }
    }

    /**
     * 获取用户角色
     */
    private String getUserRole(UserDetails userDetails) {
        try {
            if (userDetails != null && userDetails.getAuthorities() != null) {
                for (var authority : userDetails.getAuthorities()) {
                    String authorityStr = authority.getAuthority();
                    if (authorityStr.startsWith("ROLE_")) {
                        return authorityStr.substring(5);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取用户角色异常", e);
        }
        return "UNKNOWN";
    }

    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            logger.error("获取HttpServletRequest异常", e);
        }
        return null;
    }

    /**
     * 获取操作动作名称（英文）
     */
    private String getActionName(ProceedingJoinPoint joinPoint) {
        try {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            return className + "." + methodName;
        } catch (Exception e) {
            logger.error("获取操作动作名称异常", e);
            return "unknown";
        }
    }

    /**
     * 获取操作动作名称（中文）
     */
    private String getChineseActionName(ProceedingJoinPoint joinPoint) {
        try {
            // 检查方法是否有@OperationLog注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 获取目标对象的实际方法，避免获取到接口方法导致注解为null
            Method realMethod = joinPoint.getTarget()
                    .getClass()
                    .getMethod(method.getName(), method.getParameterTypes());
            
            com.example.futurecommunity.common.annotation.OperationLog operationLogAnnotation = realMethod.getAnnotation(com.example.futurecommunity.common.annotation.OperationLog.class);
            
            if (operationLogAnnotation != null) {
                // 如果有注解，使用注解中的action
                return operationLogAnnotation.action();
            }
            
            // 否则使用默认的方法名映射
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            
            // 控制器名称映射
            String controllerName = className.replace("Controller", "");
            String chineseControllerName = getChineseControllerName(controllerName);
            
            // 方法名称映射
            String chineseMethodName = getChineseMethodName(methodName);
            
            return chineseControllerName + chineseMethodName;
        } catch (Exception e) {
            logger.error("获取中文操作动作名称异常", e);
            return getActionName(joinPoint);
        }
    }

    /**
     * 获取控制器中文名称
     */
    private String getChineseControllerName(String controllerName) {
        switch (controllerName) {
            case "User":
                return "用户";
            case "Auth":
                return "认证";
            case "Repair":
                return "报修";
            case "Notice":
                return "通知";
            case "Activity":
                return "活动";
            case "Appointment":
                return "预约";
            case "Complaint":
                return "投诉";
            case "Express":
                return "快递";
            case "Health":
                return "健康";
            case "NeighborMessage":
                return "邻里消息";
            case "ResidentHouse":
                return "居民房产";
            case "RiskEvent":
                return "风险事件";
            default:
                return controllerName;
        }
    }

    /**
     * 获取方法中文名称
     */
    private String getChineseMethodName(String methodName) {
        switch (methodName) {
            case "create":
                return "创建";
            case "add":
                return "新增";
            case "save":
                return "保存";
            case "update":
                return "更新";
            case "edit":
                return "编辑";
            case "delete":
                return "删除";
            case "remove":
                return "移除";
            case "updateUser":
                return "更新";
            case "list":
                return "列表";
            case "page":
                return "分页";
            case "get":
                return "获取";
            case "find":
                return "查询";
            case "login":
                return "登录";
            case "logout":
                return "登出";
            case "register":
                return "注册";
            case "resetPassword":
                return "重置密码";
            case "inbound":
                return "快递入库";
            case "pickup":
                return "取件";
            case "reportRisk":
                return "上报风险";
            case "recognizeRisk":
                return "AI识别";
            case "handleRisk":
                return "处理风险";
            case "updateRiskStatus":
                return "更新状态";
            case "getUserByPhone":
                return "手机号查询";
            default:
                return methodName;
        }
    }

    /**
     * 获取请求参数的中文描述
     */
    private String getChineseParams(Object[] args) {
        try {
            if (args == null || args.length == 0) {
                return "无参数";
            }
            
            StringBuilder paramsBuilder = new StringBuilder();
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                
                // 跳过 HttpServletRequest 和 HttpServletResponse 等对象
                String className = arg.getClass().getName();
                if (className.contains("HttpServletRequest") || className.contains("HttpServletResponse")) {
                    continue;
                }
                
                // 简化参数显示
                String paramStr = simplifyParam(arg);
                if (!paramStr.isEmpty()) {
                    if (paramsBuilder.length() > 0) {
                        paramsBuilder.append(", ");
                    }
                    paramsBuilder.append(paramStr);
                }
            }
            
            return paramsBuilder.length() > 0 ? paramsBuilder.toString() : "无参数";
        } catch (Exception e) {
            logger.error("获取中文参数描述异常", e);
            return Arrays.toString(args);
        }
    }

    /**
     * 简化参数显示
     */
    private String simplifyParam(Object arg) {
        try {
            String className = arg.getClass().getName();
            
            // 处理常见类型
            if (arg instanceof String) {
                return "字符串: " + arg;
            } else if (arg instanceof Integer || arg instanceof Long || arg instanceof Double) {
                return "数值: " + arg;
            } else if (arg instanceof Boolean) {
                return "布尔值: " + arg;
            } else if (className.contains("User")) {
                // 处理用户对象
                return getUserObjectDesc(arg);
            } else if (className.contains("Repair")) {
                // 处理报修对象
                return "报修对象";
            } else if (className.contains("Notice")) {
                // 处理通知对象
                return "通知对象";
            } else if (className.contains("Activity")) {
                // 处理活动对象
                return "活动对象";
            } else if (className.contains("Appointment")) {
                // 处理预约对象
                return "预约对象";
            } else {
                // 其他对象
                return arg.getClass().getSimpleName();
            }
        } catch (Exception e) {
            return arg.toString();
        }
    }

    /**
     * 获取用户对象描述
     */
    private String getUserObjectDesc(Object arg) {
        try {
            // 使用反射获取用户对象的属性
            java.lang.reflect.Field nameField = arg.getClass().getDeclaredField("name");
            java.lang.reflect.Field phoneField = arg.getClass().getDeclaredField("phone");
            
            nameField.setAccessible(true);
            phoneField.setAccessible(true);
            
            String name = (String) nameField.get(arg);
            String phone = (String) phoneField.get(arg);
            
            // 打印调试信息
            logger.info("用户对象属性 - name: {}, phone: {}", name, phone);
            
            StringBuilder descBuilder = new StringBuilder();
            
            if (name != null && !name.isEmpty()) {
                descBuilder.append(name);
                if (phone != null && !phone.isEmpty()) {
                    descBuilder.append(" (") .append(phone) .append(")");
                }
            } else if (phone != null && !phone.isEmpty()) {
                descBuilder.append(phone);
            } else {
                descBuilder.append("用户对象");
            }
            
            return descBuilder.toString();
        } catch (Exception e) {
            logger.error("获取用户对象描述异常", e);
            // 如果反射失败，返回默认描述
            return "用户对象";
        }
    }

    /**
     * 生成友好的操作详情（人+操作+对象+什么事情）
     */
    private String generateFriendlyDetail(ProceedingJoinPoint joinPoint, OperationLog operationLog) {
        try {
            // 检查方法是否有@OperationLog注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 获取目标对象的实际方法，避免获取到接口方法导致注解为null
            Method realMethod = joinPoint.getTarget()
                    .getClass()
                    .getMethod(method.getName(), method.getParameterTypes());
            
            com.example.futurecommunity.common.annotation.OperationLog operationLogAnnotation = realMethod.getAnnotation(com.example.futurecommunity.common.annotation.OperationLog.class);
            
            // 获取当前登录用户信息
            String operatorName = getCurrentUserName();
            String operatorRole = operationLog.getRole();
            
            // 获取操作类型
            String action = operationLog.getAction();
            // 简化操作类型，移除重复的"用户"前缀
            if (action.startsWith("用户")) {
                action = action.substring(2);
            }
            
            // 构建友好的操作详情
            StringBuilder detailBuilder = new StringBuilder();
            
            // 添加操作人信息
            if (operatorName != null && !operatorName.isEmpty()) {
                if (operatorRole != null && !operatorRole.isEmpty()) {
                    switch (operatorRole) {
                        case "ADMIN":
                            detailBuilder.append("管理员");
                            break;
                        case "PROPERTY":
                            detailBuilder.append("物业");
                            break;
                        case "DOCTOR":
                            detailBuilder.append("医生");
                            break;
                        default:
                            detailBuilder.append("用户");
                    }
                } else {
                    detailBuilder.append("管理员");
                }
            } else {
                detailBuilder.append("管理员");
            }
            
            // 添加操作
            detailBuilder.append(action);
            
            // 如果有@OperationLog注解，使用注解中的信息
            if (operationLogAnnotation != null) {
                // 使用DefaultParameterNameDiscoverer获取方法参数名称
                DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
                String[] paramNames = discoverer.getParameterNames(method);
                Object[] args = joinPoint.getArgs();
                
                // 打印调试信息
                logger.info("方法参数名称: {}", Arrays.toString(paramNames));
                logger.info("方法参数值: {}", Arrays.toString(args));
                
                // 创建SpEL解析上下文
                StandardEvaluationContext context = new StandardEvaluationContext();
                
                // 将参数名称和参数值放入context
                if (paramNames != null && args != null) {
                    for (int i = 0; i < paramNames.length && i < args.length; i++) {
                        context.setVariable(paramNames[i], args[i]);
                        context.setVariable("p" + i, args[i]);
                        logger.info("设置SpEL变量: {} = {}, p{} = {}", paramNames[i], args[i], i, args[i]);
                        // 打印参数对象的详细信息
                        if (args[i] != null) {
                            logger.info("参数 {} 对象类型: {}, toString: {}", i, args[i].getClass().getSimpleName(), args[i]);
                        }
                    }
                }
                
                // 使用ExpressionParser解析
                ExpressionParser parser = new SpelExpressionParser();
                String target = operationLogAnnotation.target();
                String detail = operationLogAnnotation.detail();
                
                logger.info("原始target表达式: {}", target);
                logger.info("原始detail表达式: {}", detail);
                
                try {
                    // 解析target
                    if (!target.isEmpty()) {
                        try {
                            Object targetValue = parser.parseExpression(target).getValue(context);
                            logger.info("解析后的target值: {}", targetValue);
                            if (targetValue != null) {
                                target = targetValue.toString();
                            } else {
                                logger.warn("SpEL解析结果为null，开始尝试fallback逻辑");
                                // 如果解析结果为null，尝试获取用户对象的其他属性
                                for (int i = 0; i < paramNames.length && i < args.length; i++) {
                                    Object arg = args[i];
                                    logger.info("检查参数[{}]: 类型={}, 值={}", i, arg != null ? arg.getClass().getName() : "null", arg);
                                    if (arg != null && arg.getClass().getName().contains("User")) {
                                        // 尝试通过反射获取用户姓名
                                        try {
                                            java.lang.reflect.Field nameField = arg.getClass().getDeclaredField("name");
                                            nameField.setAccessible(true);
                                            String name = (String) nameField.get(arg);
                                            logger.info("通过反射获取到的用户姓名: {}", name);
                                            if (name != null && !name.isEmpty()) {
                                                target = name;
                                                logger.info("通过反射获取的用户姓名: {}", name);
                                                break;
                                            } else {
                                                logger.warn("反射获取的name字段为空，尝试通过ID查询");
                                                // 如果name为空，尝试通过User对象中的id字段查询
                                                try {
                                                    java.lang.reflect.Field idField = arg.getClass().getDeclaredField("id");
                                                    idField.setAccessible(true);
                                                    Long userId = (Long) idField.get(arg);
                                                    if (userId != null) {
                                                        User user = userService.getById(userId);
                                                        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                                                            target = user.getName();
                                                            logger.info("通过User对象ID查询获取的用户姓名: {}", target);
                                                            break;
                                                        }
                                                    }
                                                } catch (Exception ex) {
                                                    logger.warn("通过User对象ID查询用户姓名失败: {}", ex.getMessage());
                                                }
                                            }
                                        } catch (Exception ex) {
                                            logger.warn("通过反射获取用户姓名失败: {}", ex.getMessage(), ex);
                                        }
                                    } else if (arg != null && (paramNames[i].equals("id") || paramNames[i].equals("userId"))) {
                                        // 尝试通过ID查询用户信息
                                        try {
                                            Long userId = null;
                                            if (arg instanceof Integer) {
                                                userId = ((Integer) arg).longValue();
                                            } else if (arg instanceof Long) {
                                                userId = (Long) arg;
                                            }
                                            if (userId != null) {
                                                User user = userService.getById(userId);
                                                if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                                                    target = user.getName();
                                                    logger.info("通过ID查询获取的用户姓名: {}", target);
                                                    break;
                                                }
                                            }
                                        } catch (Exception ex) {
                                            logger.warn("通过ID查询获取用户姓名失败: {}", ex.getMessage());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // 如果解析异常，将target置为空字符串
                            logger.warn("SpEL解析异常，将target置为空字符串: {}", e.getMessage(), e);
                            target = "";
                        }
                    }
                    
                    // 解析detail
                    if (!detail.isEmpty()) {
                        // 只对包含#的字符串进行SpEL解析
                        if (detail.contains("#")) {
                            Object detailValue = parser.parseExpression(detail).getValue(context);
                            logger.info("解析后的detail值: {}", detailValue);
                            if (detailValue != null) {
                                detail = detailValue.toString();
                            }
                        }
                    }
                } catch (Exception e) {
                    // 如果解析失败，使用原始值
                    logger.warn("SpEL解析异常，使用原始值: {}", e.getMessage());
                }
                
                if (!target.isEmpty()) {
                    detailBuilder.append("了").append(target);
                }
                
                if (!detail.isEmpty()) {
                    detailBuilder.append("的").append(detail);
                }
            } else {
                // 否则获取操作对象
                Object[] args = joinPoint.getArgs();
                String targetObject = "";
                String targetObjectName = "";
                
                for (Object arg : args) {
                    if (arg != null) {
                        String className = arg.getClass().getName();
                        logger.info("参数类型: {}", className);
                        if (className.contains("User")) {
                            // 处理用户对象
                            logger.info("发现用户对象，开始处理");
                            targetObject = getUserObjectDesc(arg);
                            // 提取用户姓名
                            targetObjectName = extractUserName(arg);
                            // 打印调试信息
                            logger.info("用户对象姓名: {}", targetObjectName);
                            break;
                        } else if (arg instanceof Integer || arg instanceof Long) {
                            // 处理ID参数
                            targetObject = "ID: " + arg;
                            logger.info("发现ID参数: {}", targetObject);
                        }
                    } else {
                        logger.info("参数为null");
                    }
                }
                
                // 添加操作对象
                if (!targetObjectName.isEmpty()) {
                    detailBuilder.append("了用户").append(targetObjectName);
                } else if (!targetObject.isEmpty()) {
                    detailBuilder.append("了").append(targetObject);
                }
                
                // 添加具体事情
                if (action.contains("创建")) {
                    detailBuilder.append("的信息");
                } else if (action.contains("更新")) {
                    detailBuilder.append("的信息");
                } else if (action.contains("删除")) {
                    detailBuilder.append("的账户");
                } else {
                    detailBuilder.append("的信息");
                }
            }
            
            return detailBuilder.toString();
        } catch (Exception e) {
            logger.error("生成友好操作详情异常", e);
            // 回退到原始参数描述
            return getChineseParams(joinPoint.getArgs());
        }
    }

    /**
     * 提取用户姓名
     */
    private String extractUserName(Object arg) {
        try {
            // 打印调试信息
            logger.info("提取用户姓名，对象类型: {}", arg.getClass().getName());
            
            // 使用反射获取用户对象的姓名属性
            java.lang.reflect.Field nameField = arg.getClass().getDeclaredField("name");
            nameField.setAccessible(true);
            String name = (String) nameField.get(arg);
            
            // 打印调试信息
            logger.info("提取用户姓名，结果: {}", name);
            
            return name != null && !name.isEmpty() ? name : "";
        } catch (Exception e) {
            logger.error("提取用户姓名异常", e);
            return "";
        }
    }

    /**
     * 获取当前登录用户姓名
     */
    private String getCurrentUserName() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    String username = userDetails.getUsername();
                    return username;
                }
            }
        } catch (Exception e) {
            logger.error("获取当前用户姓名异常", e);
        }
        return null;
    }

    /**
     * 保存操作日志
     */
    private void saveOperationLog(OperationLog operationLog) {
        try {
            operationLogService.save(operationLog);
            logger.info("操作日志保存成功: {}", operationLog.getAction());
        } catch (Exception e) {
            logger.error("保存操作日志异常", e);
        }
    }
}
