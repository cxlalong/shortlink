package org.example.shortlink.project.util;


import jakarta.servlet.http.HttpServletRequest;

public class IpAddressUtil {

    public static String getUserIp(HttpServletRequest request) {
        // 检查 X-Forwarded-For 头部
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unKnown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For 可能包含多个 IP 地址，取第一个非未知的有效 IP 地址
            int firstUnknown = xForwardedFor.indexOf("unknown");
            if (firstUnknown > 0) {
                xForwardedFor = xForwardedFor.substring(0, firstUnknown);
            }
            String[] ips = xForwardedFor.split(",");
            for (String ip : ips) {
                String trimmedIp = ip.trim();
                if (!"unknown".equalsIgnoreCase(trimmedIp)) {
                    return trimmedIp;
                }
            }
        }

        // 检查 X-Real-IP 头部
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unKnown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        // 检查 Proxy-Client-IP 和 WL-Proxy-Client-IP 头部
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty() && !"unKnown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }

        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty() && !"unKnown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }

        // 使用 getRemoteAddr() 作为最后的选择
        return request.getRemoteAddr();
    }
}