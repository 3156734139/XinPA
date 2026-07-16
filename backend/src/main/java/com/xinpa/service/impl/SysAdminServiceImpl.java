package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.entity.SysAdmin;
import com.xinpa.mapper.SysAdminMapper;
import com.xinpa.service.SysAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理员服务实现
 */
@Service
@RequiredArgsConstructor
public class SysAdminServiceImpl implements SysAdminService {

    private final SysAdminMapper sysAdminMapper;

    @Override
    public SysAdmin getByUsername(String username) {
        return sysAdminMapper.selectOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, username)
                .eq(SysAdmin::getDeleted, 0));
    }

    @Override
    public SysAdmin getById(Long id) {
        return sysAdminMapper.selectById(id);
    }

    @Override
    public void updateLastLogin(Long id) {
        SysAdmin admin = new SysAdmin();
        admin.setId(id);
        admin.setLastLoginTime(LocalDateTime.now());
        sysAdminMapper.updateById(admin);
    }
}
