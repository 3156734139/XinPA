package com.xinpa.controller;

import com.xinpa.common.BusinessException;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.AiCallLog;
import com.xinpa.entity.SysUser;
import com.xinpa.mapper.AiCallLogMapper;
import com.xinpa.mapper.SysUserMapper;
import com.xinpa.service.AiCallLogService;
import com.xinpa.util.AiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 辅助工具接口
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiClient aiClient;
    private final AiCallLogService aiCallLogService;

    /**
     * 检查AI使用次数
     */
    @GetMapping("/quota")
    public Result<Map<String, Object>> quota() {
        Long userId = UserContext.getUserId();
        Map<String, Object> result = aiCallLogService.getDailyQuota(userId);
        return Result.ok(result);
    }

    /**
     * 生成主页简介
     */
    @PostMapping("/generate-intro")
    public Result<String> generateIntro(@RequestBody Map<String, String> params) {
        checkQuota();
        String result = aiClient.generateIntro(
                params.getOrDefault("games", ""),
                params.getOrDefault("templateType", "游戏陪玩"));
        aiCallLogService.logCall("intro", params.toString(), result, true, null);
        return Result.ok(result);
    }

    /**
     * 生成开场白
     */
    @PostMapping("/generate-opening")
    public Result<String> generateOpening(@RequestBody Map<String, String> params) {
        checkQuota();
        String result = aiClient.generateOpeningLine(
                params.getOrDefault("nickname", ""),
                params.getOrDefault("style", "亲切"));
        aiCallLogService.logCall("opening", params.toString(), result, true, null);
        return Result.ok(result);
    }

    /**
     * 生成安抚话术
     */
    @PostMapping("/generate-comfort")
    public Result<String> generateComfort(@RequestBody Map<String, String> params) {
        checkQuota();
        String result = aiClient.generateComfort(
                params.getOrDefault("customerName", ""),
                params.getOrDefault("reason", ""));
        aiCallLogService.logCall("comfort", params.toString(), result, true, null);
        return Result.ok(result);
    }

    /**
     * 生成砍价应对
     */
    @PostMapping("/generate-bargain")
    public Result<String> generateBargain(@RequestBody Map<String, String> params) {
        checkQuota();
        String result = aiClient.generateBargainResponse(
                params.getOrDefault("packageName", ""),
                params.getOrDefault("price", ""));
        aiCallLogService.logCall("bargain", params.toString(), result, true, null);
        return Result.ok(result);
    }

    /**
     * 生成活动方案
     */
    @PostMapping("/generate-activity")
    public Result<String> generateActivity(@RequestBody Map<String, String> params) {
        checkQuota();
        String result = aiClient.generateActivityPlan(
                params.getOrDefault("activityType", "周末活动"));
        aiCallLogService.logCall("activity", params.toString(), result, true, null);
        return Result.ok(result);
    }

    /**
     * 聊天风险识别
     */
    @PostMapping("/risk-detection")
    public Result<String> riskDetection(@RequestBody Map<String, String> params) {
        checkQuota();
        String result = aiClient.riskDetection(params.getOrDefault("content", ""));
        aiCallLogService.logCall("risk", params.toString(), result, true, null);
        return Result.ok(result);
    }

    /**
     * 检查AI调用配额
     */
    private void checkQuota() {
        Map<String, Object> quota = aiCallLogService.getDailyQuota(UserContext.getUserId());
        if (quota.containsKey("exceeded") && (Boolean) quota.get("exceeded")) {
            throw new BusinessException("今日AI生成次数已用完，升级会员可无限使用");
        }
    }
}
