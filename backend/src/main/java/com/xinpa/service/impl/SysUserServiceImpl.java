package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.SysUser;
import com.xinpa.entity.UserFinanceSetting;
import com.xinpa.mapper.SysUserMapper;
import com.xinpa.mapper.UserFinanceSettingMapper;
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
    private final UserFinanceSettingMapper userFinanceSettingMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser getByPhone(String phone) {
        return sysUserMapper.getByPhone(phone);
    }

    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser registerByPhone(String phone, String nickname, String password) {
        if (getByPhone(phone) != null) {
            throw new BusinessException("该手机号已注册");
        }

        SysUser user = new SysUser();
        user.setPhone(phone);
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setNickname(nickname != null && !nickname.isEmpty() ? nickname : phone);
        user.setMemberType(0);
        user.setStatus(1);
        sysUserMapper.insert(user);

        initNewUser(user.getId());
        return user;
    }

    /**
     * 新用户初始化：创建财务设置
     */
    private void initNewUser(Long userId) {
        UserFinanceSetting financeSetting = new UserFinanceSetting();
        financeSetting.setUserId(userId);
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
