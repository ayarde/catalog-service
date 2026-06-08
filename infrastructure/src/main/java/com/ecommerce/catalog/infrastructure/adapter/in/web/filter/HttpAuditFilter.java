package com.ecommerce.catalog.infrastructure.adapter.in.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Filtro de Auditoría HTTP para registrar peticiones y respuestas.
 * Utiliza Wrappers para poder leer el cuerpo del mensaje sin afectar el flujo normal.
 */
@Component
public class HttpAuditFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpAuditFilter.class);
    private static final int MAX_PAYLOAD_LENGTH = 1024;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isAsyncDispatch(request) || isManagementPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequestResponse(requestWrapper, responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private boolean isManagementPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/management") || uri.contains("swagger") || uri.contains("api-docs");
    }

    private void logRequestResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        int status = response.getStatus();

        String requestBody = getPayload(request.getContentAsByteArray(), request.getCharacterEncoding());
        String responseBody = getPayload(response.getContentAsByteArray(), response.getCharacterEncoding());

        log.info("HTTP Audit | {} {}{} | Status: {} | Duration: {}ms | Request: {} | Response: {}",
                method, uri, queryString, status, duration, requestBody, responseBody);
    }

    private String getPayload(byte[] buf, String encoding) {
        if (buf == null || buf.length == 0) {
            return "[empty]";
        }
        try {
            String enc = (encoding != null) ? encoding : "UTF-8";
            int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH);
            String payload = new String(buf, 0, length, enc);
            payload = payload.replace("\n", " ").replace("\r", " ").trim();
            return buf.length > MAX_PAYLOAD_LENGTH ? payload + "..." : payload;
        } catch (UnsupportedEncodingException e) {
            return "[unknown encoding]";
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return isManagementPath(request);
    }
}
