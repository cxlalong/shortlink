package org.example.shortlink.project.util;

import jakarta.servlet.http.HttpServletRequest;

public class UserAgentUtils {

    /**
     * 获取请求的浏览器名称。
     *
     * @param request HttpServletRequest 对象
     * @return 浏览器名称字符串
     */
    public static String getBrowser(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }

        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        return parseBrowser(userAgent);
    }

    /**
     * 解析 User-Agent 字符串以获取浏览器名称。
     *
     * @param userAgent User-Agent 字符串
     * @return 浏览器名称字符串
     */
    private static String parseBrowser(String userAgent) {
        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("edge")) {
            return "Edge";
        } else if (userAgent.contains("chrome") && !userAgent.contains("chromium")) {
            return "Chrome";
        } else if (userAgent.contains("safari") && !userAgent.contains("chrome") && !userAgent.contains("chromium")) {
            return "Safari";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("opera") || userAgent.contains("opr")) {
            return "Opera";
        } else if (userAgent.contains("trident") || userAgent.contains("msie")) {
            return "Internet Explorer";
        } else if (userAgent.contains("chromium")) {
            return "Chromium";
        } else {
            return "Unknown";
        }
    }

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

    /**
     * 获取用户访问设备
     *
     * @param request 请求
     * @return 访问设备
     */
    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("mobile")) {
            return "Mobile";
        }
        return "PC";
    }
}