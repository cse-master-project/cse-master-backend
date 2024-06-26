package com.example.csemaster;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        ContentCachingRequestWrapper httpServletRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        String requestURI = httpServletRequest.getRequestURI();
        String requestID = UUID.randomUUID().toString();
        String clientIP = request.getRemoteAddr();

        MDC.put("traceId", requestID);
        try {
            log.info("Request URI : '{}', from IP : {}", requestURI, clientIP);

            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // log.info("Response [{}]", requestURI);
            MDC.clear();
        }
    }
}