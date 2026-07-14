package de.sassenberger.mcp.server.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Records every message exchanged over the MCP endpoint into the
 * {@link McpTrafficLog}. POST (JSON-RPC messages) and DELETE (session close)
 * bodies are captured with caching wrappers; GET requests open long-lived SSE
 * listening streams and are logged without body buffering so streaming is not
 * broken.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class McpTrafficFilter extends OncePerRequestFilter {

    private static final int MAX_CACHED_BODY_BYTES = 256 * 1024;

    private final McpTrafficLog trafficLog;

    public McpTrafficFilter(McpTrafficLog trafficLog) {
        this.trafficLog = trafficLog;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/mcp");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!"POST".equals(request.getMethod()) && !"DELETE".equals(request.getMethod())) {
            long start = System.nanoTime();
            try {
                filterChain.doFilter(request, response);
            }
            finally {
                trafficLog.record(request.getMethod(), request.getRequestURI(), response.getStatus(),
                        elapsedMs(start), sessionId(request, response),
                        "", "(SSE listening stream — body not captured)");
            }
            return;
        }

        ContentCachingRequestWrapper requestWrapper =
                new ContentCachingRequestWrapper(request, MAX_CACHED_BODY_BYTES);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        long start = System.nanoTime();
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        }
        finally {
            String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            trafficLog.record(request.getMethod(), request.getRequestURI(), responseWrapper.getStatus(),
                    elapsedMs(start), sessionId(request, responseWrapper), requestBody, responseBody);
            responseWrapper.copyBodyToResponse();
        }
    }

    private static String sessionId(HttpServletRequest request, HttpServletResponse response) {
        String fromResponse = response.getHeader("Mcp-Session-Id");
        return fromResponse != null ? fromResponse : request.getHeader("Mcp-Session-Id");
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
