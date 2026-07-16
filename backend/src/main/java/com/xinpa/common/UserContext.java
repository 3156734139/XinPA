package com.xinpa.common;

/**
 * 用户上下文，存储当前登录用户ID
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_TYPE = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUserType(String userType) {
        USER_TYPE.set(userType);
    }

    public static String getUserType() {
        return USER_TYPE.get();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(USER_TYPE.get());
    }

    public static void clear() {
        USER_ID.remove();
        USER_TYPE.remove();
    }
}
