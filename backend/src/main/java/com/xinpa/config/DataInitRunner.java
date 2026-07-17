package com.xinpa.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.entity.SysAdmin;
import com.xinpa.mapper.SysAdminMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitRunner implements CommandLineRunner {

    private final SysAdminMapper sysAdminMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        SysAdmin admin = sysAdminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getUsername, "admin"));
        if (admin == null) {
            SysAdmin newAdmin = new SysAdmin();
            newAdmin.setUsername("admin");
            newAdmin.setPassword(passwordEncoder.encode("admin123"));
            newAdmin.setRole("SUPER_ADMIN");
            sysAdminMapper.insert(newAdmin);
            log.info("初始化管理员账号: admin");
        }
    }
}
