package com.xinpa.service;

import java.util.Map;

/**
 * AI 调用日志服务接口
 */
public interface AiCallLogService {

    /**
     * 记录AI调用
     */
    void logCall(String scene, String prompt, String response, boolean success, String errorMsg);

    /**
     * 查询每日配额使用情况
     */
    Map<String, Object> getDailyQuota(Long userId);
}
