package com.parkeasy.web;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String RESPONSE_HEADER = "X-Correlation-ID";
    private static final String REQUEST_ATTRIBUTE = RequestCorrelationFilter.class.getName() + ".correlationId";
    private static final String LOGGING_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String correlationId = UUID.randomUUID().toString();
        request.setAttribute(REQUEST_ATTRIBUTE, correlationId);
        response.setHeader(RESPONSE_HEADER, correlationId);

        try (MDC.MDCCloseable ignored = MDC.putCloseable(LOGGING_KEY, correlationId)) {
            filterChain.doFilter(request, response);
        }
    }

    public static String correlationId(HttpServletRequest request) {
        Object value = request.getAttribute(REQUEST_ATTRIBUTE);
        return value == null ? "unavailable" : value.toString();
    }
}
