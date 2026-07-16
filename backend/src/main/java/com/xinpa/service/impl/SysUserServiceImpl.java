package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.SysUser;
import com.xinpa.entity.UserFinanceSetting;
import com.xinpa.entity.UserProfile;
import com.xinpa.mapper.SysUserMapper;
import com.xinpa.mapper.UserFinanceSettingMapper;
import com.xinpa.mapper.UserProfileMapper;
import com.xinpa.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserFinanceSettingMapper userFinanceSettingMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0));
    }

    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password, String nickname) {
        if (getByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setMemberType(0);
        user.setStatus(1);
        sysUserMapper.insert(user);

        // 初始化人设主页
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setTemplateType(1);
        profile.setOrderStatus(1);
        userProfileMapper.insert(profile);

        // 初始化财务设置
        UserFinanceSetting financeSetting = new UserFinanceSetting();
        financeSetting.setUserId(user.getId());
        financeSetting.setMonthlyTarget(BigDecimal.ZERO);
        financeSetting.setWithdrawFeeRate(BigDecimal.valueOf(0.006));
        userFinanceSettingMapper.insert(financeSetting);
    }

    @Override
    public void updateLastLogin(Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Override
    public void updateInfo(SysUser user) {
        sysUserMapper.updateById(user);
    }
}
