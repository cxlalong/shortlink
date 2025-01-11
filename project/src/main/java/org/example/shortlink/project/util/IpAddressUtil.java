package org.example.shortlink.project.util;


import jakarta.servlet.http.HttpServletRequest;
import org.apache.curator.shaded.com.google.common.base.Strings;

public class IpAddressUtil {

    public static String getUserIp(HttpServletRequest request) {
        String Xip = request.getHeader("X-Real-IP");
        String XFor = request.getHeader("X-Forwarded-For");

        if (!Strings.isNullOrEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = XFor.indexOf(",");
            if (index != -1) {
                return XFor.substring(0, index);
            } else {
                return XFor;
            }
        }
        XFor = Xip;
        if (!Strings.isNullOrEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            return XFor;
        }
        if (Strings.nullToEmpty(XFor).trim().isEmpty() || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("Proxy-Client-IP");
        }
        if (Strings.nullToEmpty(XFor).trim().isEmpty() || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (Strings.nullToEmpty(XFor).trim().isEmpty() || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (Strings.nullToEmpty(XFor).trim().isEmpty() || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (Strings.nullToEmpty(XFor).trim().isEmpty() || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getRemoteAddr();
        }
        return XFor;
    }

}