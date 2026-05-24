package com.neu.webapp.security;

public class UserContext {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();

    public static void set(Long userId, String username, String role) {
        userIdHolder.set(userId);
        usernameHolder.set(username);
        roleHolder.set(role);
    }

    public static Long getCurrentUserId() {
        return userIdHolder.get();
    }

    public static String getCurrentUsername() {
        return usernameHolder.get();
    }

    public static String getCurrentRole() {
        return roleHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
        usernameHolder.remove();
        roleHolder.remove();
    }
}
