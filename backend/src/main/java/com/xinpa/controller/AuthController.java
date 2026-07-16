package com.xinpa.controller;

import com.xinpa.common.BusinessException;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.dto.LoginRequest;
import com.xinpa.dto.RegisterRequest;
import com.xinpa.entity.SysUser;
import com.xinpa.security.JwtUtils;
import com.xinpa.service.SysUserService;
import com.xinpa.vo.LoginVO;
import com.xinpa.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * 用户认证接口
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload-dir:uploads/avatars}")
    private String uploadDir;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        SysUser user = sysUserService.getByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(401, "账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        sysUserService.updateLastLogin(user.getId());
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), "USER");
        return Result.ok(new LoginVO(token, user.getNickname(), user.getId(), user.getMemberType()));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        sysUserService.register(request.getUsername(), request.getPassword(), request.getNickname());
        return Result.ok();
    }

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
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setMemberType(user.getMemberType());
        vo.setMemberExpire(user.getMemberExpire());
        vo.setLastLoginTime(user.getLastLoginTime());
        return Result.ok(vo);
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/me")
    public Result<Void> updateMe(@RequestBody SysUser user) {
        Long userId = com.xinpa.common.UserContext.getUserId();
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
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        if (newPassword.length() < 6) {
            throw new BusinessException("新密码至少6位");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        sysUserService.updateInfo(update);
        return Result.ok();
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择头像文件");
        }
        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + ext;
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path target = dir.resolve(fileName);
            file.transferTo(target.toFile());

            String avatarUrl = "/api/uploads/avatars/" + fileName;

            // 更新用户头像
            Long userId = UserContext.getUserId();
            SysUser update = new SysUser();
            update.setId(userId);
            update.setAvatar(avatarUrl);
            sysUserService.updateInfo(update);

            return Result.ok(Map.of("url", avatarUrl));
        } catch (IOException e) {
            throw new BusinessException("头像上传失败: " + e.getMessage());
        }
    }
}
