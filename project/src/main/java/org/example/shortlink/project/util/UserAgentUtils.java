package org.example.shortlink.project.util;

import jakarta.servlet.http.HttpServletRequest;

public class UserAgentUtils {

    /**
     * 获取请求的操作系统名称。
     *
     * @param request HttpServletRequest 对象
     * @return 操作系统名称字符串
     */
    public static String getOperatingSystem(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }

        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        return parseOperatingSystem(userAgent);
    }

    /**
     * 解析 User-Agent 字符串以获取操作系统名称。
     *
     * @param userAgent User-Agent 字符串
     * @return 操作系统名称字符串
     */
    private static String parseOperatingSystem(String userAgent) {
        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac") || userAgent.contains("darwin")) {
            return "MacOS";
        } else if (userAgent.contains("x11") || userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad") || userAgent.contains("ipod")) {
            return "iOS";
        } else if (userAgent.contains("unix")) {
            return "Unix";
        } else if (userAgent.contains("bsd")) {
            return "BSD";
        } else {
            return "Unknown";
        }
    }
}