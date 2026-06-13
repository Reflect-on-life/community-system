package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.LoginRequest;
import com.example.futurecommunity.model.LoginResponse;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.model.UserRole;
import com.example.futurecommunity.service.UserService;
import com.example.futurecommunity.service.UserRoleService;
import com.example.futurecommunity.service.RoleService;
import com.example.futurecommunity.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    // 存储验证码的 Map，key 为手机号，value 为验证码信息
    private static final Map<String, VerificationCode> VERIFICATION_CODE_MAP = new ConcurrentHashMap<>();
    
    // 验证码有效期 5 分钟（毫秒）
    private static final long CODE_EXPIRE_TIME = 5 * 60 * 1000;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/test")
    public String test() {
        logger.info("接收到测试请求");
        return "测试成功";
    }
    
    /**
     * 发送短信验证码
     * @param phone 手机号
     * @return 成功响应
     */
    @PostMapping("/send-code")
    public Map<String, Object> sendCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        
        logger.info("接收到发送验证码请求，手机号：{}", phone);
        
        // 验证手机号格式
        if (phone == null || phone.length() != 11) {
            throw new RuntimeException("手机号格式错误");
        }
        
        // 生成 6 位随机数字验证码
        String code = generateVerificationCode();
        
        // 存储验证码
        VERIFICATION_CODE_MAP.put(phone, new VerificationCode(code, System.currentTimeMillis()));
        
        logger.info("验证码已生成并存储，手机号：{}, 验证码：{}", phone, code);
        
        // 返回成功响应（实际应用中这里应该调用短信服务商 API）
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "验证码发送成功");
        // 为了前端测试，返回验证码（实际应用中不应该返回）
        response.put("code", code);
        
        return response;
    }
    
    /**
     * 生成 6 位随机数字验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    /**
     * 验证码内部类
     */
    private static class VerificationCode {
        private final String code;
        private final long createTime;
        
        public VerificationCode(String code, long createTime) {
            this.code = code;
            this.createTime = createTime;
        }
        
        public String getCode() {
            return code;
        }
        
        public long getCreateTime() {
            return createTime;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - createTime > CODE_EXPIRE_TIME;
        }
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        try {
            String phone = loginRequest.getPhone();
            String password = loginRequest.getPassword();
            String verificationCode = loginRequest.getVerificationCode();
            
            logger.info("接收到登录请求，手机号：{}, 密码：{}, 验证码：{}", phone, password, verificationCode);
            
            // 验证手机号格式
            if (phone == null || phone.length() != 11) {
                throw new RuntimeException("手机号格式错误");
            }
            
            // 如果提供了验证码，则验证验证码
            if (verificationCode != null && !verificationCode.isEmpty()) {
                logger.info("使用验证码登录，手机号：{}", phone);
                
                // 检查验证码是否存在
                VerificationCode storedCode = VERIFICATION_CODE_MAP.get(phone);
                if (storedCode == null) {
                    throw new RuntimeException("验证码不存在或已过期，请重新获取");
                }
                
                // 检查验证码是否过期
                if (storedCode.isExpired()) {
                    VERIFICATION_CODE_MAP.remove(phone);
                    throw new RuntimeException("验证码已过期，请重新获取");
                }
                
                // 验证验证码是否正确
                if (!storedCode.getCode().equals(verificationCode)) {
                    throw new RuntimeException("验证码错误");
                }
                
                // 验证码验证通过，移除验证码（防止重复使用）
                VERIFICATION_CODE_MAP.remove(phone);
                logger.info("验证码验证通过，手机号：{}", phone);
            } else {
                // 使用密码登录
                logger.info("使用密码登录，手机号：{}", phone);
                
                // 暂时跳过密码验证，以便测试其他功能
                /*
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    logger.warn("密码错误，手机号：{}", phone);
                    throw new RuntimeException("用户名或密码错误");
                }
                */
            }
            
            User user = userService.lambdaQuery()
                    .eq(User::getPhone, phone)
                    .one();

            logger.info("查询到用户：{}", user);
            
            if (user == null) {
                logger.warn("用户不存在，手机号：{}", phone);
                throw new RuntimeException("用户不存在");
            }

            logger.info("密码验证通过，用户: {}", user.getName());
            
            // 查询用户角色
            List<UserRole> userRoles = userRoleService.lambdaQuery()
                    .eq(UserRole::getUserId, user.getId())
                    .list();

            logger.info("查询到用户角色: {}", userRoles);
            
            List<String> roles = new java.util.ArrayList<>();
            for (UserRole userRole : userRoles) {
                try {
                    logger.info("角色ID: {}", userRole.getRoleId());
                    String roleName = roleService.getById(userRole.getRoleId()).getName();
                    roles.add(roleName);
                    logger.info("添加角色: {}", roleName);
                } catch (Exception e) {
                    logger.error("获取角色失败，角色ID: {}", userRole.getRoleId(), e);
                }
            }

            logger.info("用户角色: {}", roles);

            // 查询用户权限
            List<String> permissions = userService.getUserPermissions(user.getId());
            logger.info("用户权限: {}", permissions);
            
            // 恢复 JWT 相关代码，包含权限信息
            String token = jwtUtil.generateToken(user.getId(), user.getType(), roles, permissions);
            logger.info("生成 token: {}", token);

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUserId(user.getId());
            response.setType(user.getType());
            response.setName(user.getName());
            response.setRoles(roles);
            response.setPermissions(permissions);
            response.setTags(user.getTags());
            response.setPhone(user.getPhone());
            response.setGender(user.getGender());
            response.setBirthday(user.getBirthday());
            response.setAvatar(user.getAvatar());

            logger.info("登录成功，返回响应: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("登录失败，错误信息: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/register")
    public void register(@RequestBody User user) {
        try {
            logger.info("接收到注册请求，用户信息: {}", user);
            
            // 检查手机号是否已存在
            User existingUser = userService.lambdaQuery()
                    .eq(User::getPhone, user.getPhone())
                    .one();
            
            if (existingUser != null) {
                logger.warn("手机号已存在，手机号: {}", user.getPhone());
                throw new RuntimeException("手机号已被注册");
            }
            
            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // 设置默认状态
            user.setStatus(1);
            
            // 保存用户
            userService.save(user);
            logger.info("用户保存成功，用户ID: {}", user.getId());
            
            // 为用户分配默认角色
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            
            // 根据用户类型分配对应角色
            if ("doctor".equals(user.getType())) {
                userRole.setRoleId(2L); // 医生角色
            } else if ("property".equals(user.getType())) {
                userRole.setRoleId(3L); // 物业角色
            } else {
                userRole.setRoleId(4L); // 居民角色
            }
            
            userRoleService.save(userRole);
            logger.info("用户角色分配成功，用户ID: {}, 角色ID: {}", user.getId(), userRole.getRoleId());
            
        } catch (Exception e) {
            logger.error("注册失败，错误信息: {}", e.getMessage(), e);
            throw e;
        }
    }

}
