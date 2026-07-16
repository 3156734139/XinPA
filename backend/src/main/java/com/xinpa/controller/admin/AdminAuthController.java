package com.xinpa.controller.admin;

import com.xinpa.common.BusinessException;
import com.xinpa.common.Result;
import com.xinpa.dto.LoginRequest;
import com.xinpa.entity.SysAdmin;
import com.xinpa.security.JwtUtils;
import com.xinpa.service.SysAdminService;
import com.xinpa.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员认证接口
 */
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final SysAdminService sysAdminService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        SysAdmin admin = sysAdminService.getByUsername(request.getUsername());
        if (admin == null) {
            throw new BusinessException(401, "管理员账号或密码错误");
        }
        if (admin.getStatus() == 0) {
            throw new BusinessException(401, "账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(401, "管理员账号或密码错误");
        }

        sysAdminService.updateLastLogin(admin.getId());
        String token = jwtUtils.generateToken(admin.getId(), admin.getUsername(), "ADMIN");
        return Result.ok(new LoginVO(token, admin.getRealName(), admin.getId(), null));
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/me")
    public Result<SysAdmin> me() {
        Long userId = com.xinpa.common.UserContext.getUserId();
        SysAdmin admin = sysAdminService.getById(userId);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        admin.setPassword(null);
        return Result.ok(admin);
    }
}
