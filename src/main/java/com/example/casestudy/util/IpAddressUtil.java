package com.example.casestudy.util;

import com.example.casestudy.exception.common.InvalidInputException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Set;

/**
 * Utility class for extracting client IP addresses from HTTP requests.
 * 
 * This utility implements IP spoofing protection by:
 * 1. Validating that requests come through the NGINX proxy
 * 2. Using X-Real-IP as the source of truth (set by NGINX to $remote_addr)
 * 3. Comparing X-Real-IP with the first IP in X-Forwarded-For chain
 * 4. Rejecting requests with mismatched IPs (spoofing attempts)
 * 5. Rejecting requests that bypass NGINX proxy
 * 
 * Network topology: UI → NGINX (localhost:90) → Spring Boot Service
 */
@Component
public class IpAddressUtil {

    private static final Logger logger = LoggerFactory.getLogger(IpAddressUtil.class);
    
    // NGINX proxy IPs (localhost)
    private static final Set<String> NGINX_PROXY_IPS = Set.of("127.0.0.1", "0:0:0:0:0:0:0:1", "::1");

    /**
     * Extracts the client IP address from the HTTP request with IP spoofing protection.
     * 
     * Security logic:
     * 1. Verify request came from NGINX proxy (127.0.0.1)
     * 2. Get X-Real-IP (NGINX sets this to $remote_addr, cannot be spoofed)
     * 3. Get X-Forwarded-For chain
     * 4. Compare X-Real-IP with first IP in X-Forwarded-For
     * 5. If mismatch or multiple IPs in chain, reject as spoofing attempt
     * 6. Return X-Real-IP (the validated client IP)
     * 
     * @param request the HTTP servlet request
     * @return the validated client IP address, never null
     * @throws InvalidInputException if request didn't come through NGINX proxy or IP spoofing is detected
     */
    public String getClientIpAddress(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        
        // Step 1: Verify request came from NGINX proxy
        if (!NGINX_PROXY_IPS.contains(remoteAddr)) {
            logger.error("SECURITY ALERT - Request did not come from NGINX proxy. Remote address: {}, X-Real-IP: {}, X-Forwarded-For: {}", 
                        remoteAddr, 
                        request.getHeader("X-Real-IP"),
                        request.getHeader("X-Forwarded-For"));
            throw new InvalidInputException("Request must come through NGINX proxy");
        }
        
        // Step 2: Get X-Real-IP (set by NGINX)
        String realIp = request.getHeader("X-Real-IP");
        String forwardedFor = request.getHeader("X-Forwarded-For");
        
        if (realIp == null || realIp.isEmpty()) {
            logger.error("SECURITY ALERT - X-Real-IP header missing from NGINX proxy request. Remote address: {}", remoteAddr);
            throw new InvalidInputException("Missing X-Real-IP header from proxy");
        }
        
        
        // Step 3: If X-Forwarded-For exists, validate it
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            String[] ips = forwardedFor.split(",");
            
            // Step 4: For UI → NGINX → Service topology, X-Forwarded-For should have exactly 1 IP
            // If it has more, someone tried to inject a fake IP before NGINX appended the real one
            if (ips.length > 1) {
                logger.error("SECURITY ALERT - IP SPOOFING DETECTED! X-Forwarded-For has {} IPs (expected 1). " +
                           "X-Real-IP: {}, X-Forwarded-For chain: {}, Request from NGINX proxy: {}", 
                           ips.length, realIp, forwardedFor, remoteAddr);
                
                throw new InvalidInputException("IP spoofing detected: multiple IPs in forwarded chain");
            }
            
            // Step 5: Compare X-Real-IP with the first (and only) IP in X-Forwarded-For
            String firstIp = ips[0].trim();
            
            if (!realIp.equals(firstIp)) {
                logger.error("SECURITY ALERT - IP MISMATCH! X-Real-IP ({}) does not match X-Forwarded-For IP ({}). " +
                           "Request from NGINX proxy: {}", 
                           realIp, firstIp, remoteAddr);
                
                throw new InvalidInputException("Header mismatch detected");
            }
            
            logger.debug("Validated client IP {} (X-Real-IP matches X-Forwarded-For)", realIp);
        } else {
            logger.debug("Using X-Real-IP: {} (no X-Forwarded-For present)", realIp);
        }
        
        // Step 6: Return X-Real-IP (the validated client IP)
        return realIp;
    }
}