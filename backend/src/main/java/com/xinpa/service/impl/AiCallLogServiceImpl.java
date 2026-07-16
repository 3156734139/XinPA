package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.entity.AiCallLog;
import com.xinpa.entity.SysUser;
import com.xinpa.mapper.AiCallLogMapper;
import com.xinpa.mapper.SysUserMapper;
import com.xinpa.service.AiCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 调用日志服务实现
 */
@Service
@RequiredArgsConstructor
public class AiCallLogServiceImpl implements AiCallLogService {

    private final AiCallLogMapper aiCallLogMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public void logCall(String scene, String prompt, String response, boolean success, String errorMsg) {
        AiCallLog log = new AiCallLog();
        log.setUserId(com.xinpa.common.UserContext.getUserId());
        log.setScene(scene);
        log.setPrompt(prompt);
        log.setResponse(response);
        log.setSuccess(success ? 1 : 0);
        log.setErrorMsg(errorMsg);
        aiCallLogMapper.insert(log);
    }

    @Override
    public Map<String, Object> getDailyQuota(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        int dailyLimit = user != null && user.getMemberType() == 1 ? 9999 : 10;

        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Long usedCount = aiCallLogMapper.selectCount(
                new LambdaQueryWrapper<AiCallLog>()
                        .eq(AiCallLog::getUserId, userId)
                        .eq(AiCallLog::getSuccess, 1)
                        .between(AiCallLog::getCreatedAt, start, end));

        Map<String, Object> result = new HashMap<>();
        result.put("dailyLimit", dailyLimit);
        result.put("usedCount", usedCount != null ? usedCount : 0);
        result.put("remaining", Math.max(0, dailyLimit - (usedCount != null ? usedCount : 0)));
        result.put("exceeded", (usedCount != null ? usedCount : 0) >= dailyLimit);
        return result;
    }
}
