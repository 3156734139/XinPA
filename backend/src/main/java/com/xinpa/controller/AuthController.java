package com.xinpa.controller;

import com.xinpa.common.BusinessException;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.dto.LoginRequest;
import com.xinpa.dto.RegisterRequest;
import com.xinpa.entity.SysUser;
import com.xinpa.security.JwtUtils;
import com.xinpa.service.SmsCodeService;
import com.xinpa.service.SysUserService;
import com.xinpa.util.OssUtil;
import com.xinpa.vo.LoginVO;
import com.xinpa.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

/**
 * 用户认证接口
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final SmsCodeService smsCodeService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final OssUtil ossUtil;

    // ==================== 发送验证码 ====================

    /**
     * 发送短信验证码
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        smsCodeService.sendCode(phone);
        return Result.ok();
    }

    // ==================== 登录 ====================

    /**
     * 用户登录（验证码登录 / 密码登录）
     * loginType=sms: 手机号+验证码
     * loginType=password: 手机号+密码
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        String phone = request.getPhone();
        SysUser user = sysUserService.getByPhone(phone);
        if (user == null) {
            throw new BusinessException(401, "该手机号未注册");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(401, "账号已被禁用");
        }

        if ("sms".equals(request.getLoginType())) {
            // 验证码登录
            if (request.getCode() == null || request.getCode().isEmpty()) {
                throw new BusinessException("请输入验证码");
            }
            boolean verified = smsCodeService.verifyCode(phone, request.getCode());
            if (!verified) {
                throw new BusinessException("验证码错误或已过期");
            }
        } else if ("password".equals(request.getLoginType())) {
            // 密码登录
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                throw new BusinessException("请输入密码");
            }
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new BusinessException("该账号未设置密码，请使用验证码登录");
            }
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BusinessException(401, "手机号或密码错误");
            }
        } else {
            throw new BusinessException("不支持的登录方式");
        }

        sysUserService.updateLastLogin(user.getId());
        String accessToken = jwtUtils.generateToken(user.getId(), "USER");
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), "USER");
        return Result.ok(new LoginVO(accessToken, refreshToken, user.getNickname(), user.getId(), user.getMemberType()));
    }

    // ==================== 注册 ====================

    /**
     * 用户注册（手机号+验证码）
     * 密码选填，不设密码只能通过验证码登录
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        // 校验验证码
        boolean verified = smsCodeService.verifyCode(request.getPhone(), request.getCode());
        if (!verified) {
            throw new BusinessException("验证码错误或已过期");
        }

        SysUser user = sysUserService.registerByPhone(request.getPhone(), request.getNickname(), request.getPassword());

        sysUserService.updateLastLogin(user.getId());
        String accessToken = jwtUtils.generateToken(user.getId(), "USER");
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), "USER");
        return Result.ok(new LoginVO(accessToken, refreshToken, user.getNickname(), user.getId(), user.getMemberType()));
    }

    // ==================== Token 刷新 ====================

    /**
     * 刷新 accessToken（使用 refreshToken）
     */
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException("refreshToken 不能为空");
        }
        var claims = jwtUtils.validateRefreshToken(refreshToken);
        if (claims == null) {
            throw new BusinessException(401, "refreshToken 无效或已过期，请重新登录");
        }
        Long userId = claims.get("userId", Long.class);
        String userType = claims.get("userType", String.class);
        String newAccessToken = jwtUtils.generateToken(userId, userType);
        return Result.ok(Map.of("token", newAccessToken));
    }

    // ==================== 用户信息 ====================

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        Long userId = com.xinpa.common.UserContext.getUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        // avatar：存储的是原始 objectKey，返回即时签名 URL（24小时有效）
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            vo.setAvatar(ossUtil.generatePresignedUrl(user.getAvatar(), 1440));
        }
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setMemberType(user.getMemberType());
        vo.setMemberExpire(user.getMemberExpire());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreatedAt(user.getCreatedAt());
        return Result.ok(vo);
    }

    /**
     * 更新个人信息（仅允许修改 nickname / phone / email）
     */
    @PutMapping("/me")
    public Result<Void> updateMe(@RequestBody SysUser user) {
        Long userId = com.xinpa.common.UserContext.getUserId();
        // 禁止通过此接口修改 username / password
        user.setPassword(null);
        // 昵称校验 2-6 字
        if (user.getNickname() != null) {
            String trimmed = user.getNickname().trim();
            if (trimmed.length() < 2 || trimmed.length() > 6) {
                throw new BusinessException("昵称长度需在2-6个字之间");
            }
            user.setNickname(trimmed);
        }
        // 手机号已绑定则禁止修改
        SysUser existing = sysUserService.getById(userId);
        if (existing != null && existing.getPhone() != null && !existing.getPhone().isEmpty()
                && user.getPhone() != null && !user.getPhone().equals(existing.getPhone())) {
            throw new BusinessException("手机号已绑定，不可修改");
        }
        user.setId(userId);
        sysUserService.updateInfo(user);
        return Result.ok();
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            throw new BusinessException("请填写旧密码和新密码");
        }
        if (newPassword.length() < 6) {
            throw new BusinessException("新密码至少6位");
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new BusinessException("旧密码不正确");
            }
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        sysUserService.updateInfo(update);
        return Result.ok();
    }

    /**
     * 获取 STS 临时上传凭证（前端直传 OSS 用）
     */
    @GetMapping("/avatar/upload-token")
    public Result<OssUtil.StsCredentials> getAvatarUploadToken() {
        Long userId = UserContext.getUserId();
        String objectKey = "avatars/" + userId + "/" + UUID.randomUUID().toString().replace("-", "");
        return Result.ok(ossUtil.generateStsCredentials(objectKey, 900L));
    }

    /**
     * 通知后端头像上传完成，更新数据库
     * 只存储原始 objectKey，前端通过 /auth/me 获取即时签名 URL
     */
    @PostMapping("/avatar/notify-complete")
    public Result<Map<String, String>> notifyAvatarComplete(@RequestBody Map<String, String> body) {
        String objectKey = body.get("objectKey");
        if (objectKey == null || objectKey.isBlank()) {
            throw new BusinessException("objectKey 不能为空");
        }
        Long userId = UserContext.getUserId();
        SysUser update = new SysUser();
        update.setId(userId);
        update.setAvatar(objectKey);
        sysUserService.updateInfo(update);
        return Result.ok(Map.of("url", objectKey));
    }

    /**
     * 后端代理上传头像到 OSS（浏览器端 ali-oss SDK 有 CORS 兼容问题，改用后端上传）
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择头像文件");
        }
        try {
            Long userId = UserContext.getUserId();
            String ext = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String objectKey = "avatars/" + userId + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
            ossUtil.uploadBytes(objectKey, file.getBytes(), file.getContentType());
            String avatarUrl = ossUtil.buildPublicUrl(objectKey);
            SysUser update = new SysUser();
            update.setId(userId);
            update.setAvatar(avatarUrl);
            sysUserService.updateInfo(update);
            return Result.ok(Map.of("url", avatarUrl));
        } catch (Exception e) {
            throw new BusinessException("头像上传失败: " + e.getMessage());
        }
    }
}
