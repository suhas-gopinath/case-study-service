package com.example.casestudy.util;

import com.example.casestudy.exception.common.InvalidInputException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class IpAddressUtil {

    private static final Logger logger = LoggerFactory.getLogger(IpAddressUtil.class);
    private static final Set<String> NGINX_PROXY_IPS = Set.of("127.0.0.1");

    public String getClientIpAddress(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String realIp = request.getHeader("X-Real-IP");
        String forwardedFor = request.getHeader("X-Forwarded-For");
        
        if (!NGINX_PROXY_IPS.contains(forwardedFor)) {
            logger.error("SECURITY ALERT - Request did not come from NGINX proxy. Remote address: {}, X-Forwarded-For: {}", 
                        remoteAddr,
                        request.getHeader("X-Forwarded-For"));
            throw new InvalidInputException("Request must come through NGINX proxy");
        }
        

        logger.info("Extracting client IP. Remote address: {}, X-Real-IP: {}, X-Forwarded-For: {}", 
                    remoteAddr, realIp, forwardedFor);
        
        return remoteAddr;
    }
}