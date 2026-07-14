package de.sassenberger.mcp.server.web;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

/**
 * In-memory log of the MCP traffic exchanged between client and server over
 * the {@code /mcp} endpoint (newest first, capped). Payloads are stored twice:
 * compacted to a single line for the collapsed view and pretty-printed for the
 * expanded view of the management UI's History tab.
 */
@Service
public class McpTrafficLog {

    private static final int MAX_ENTRIES = 500;
    private static final int MAX_BODY_CHARS = 200_000;

    private final Deque<McpExchange> exchanges = new ConcurrentLinkedDeque<>();
    private final AtomicLong sequence = new AtomicLong();
    private final ObjectMapper mapper = new ObjectMapper();

    public void record(String httpMethod, String path, int status, long durationMs,
            String sessionId, String requestBody, String responseBody) {
        McpExchange exchange = new McpExchange(
                sequence.incrementAndGet(),
                LocalDateTime.now(),
                httpMethod,
                path,
                status,
                durationMs,
                sessionId == null ? "" : sessionId,
                summarize(requestBody),
                compact(requestBody),
                pretty(requestBody),
                compact(responseBody),
                pretty(responseBody));
        exchanges.addFirst(exchange);
        while (exchanges.size() > MAX_ENTRIES) {
            exchanges.pollLast();
        }
    }

    /** Newest first. */
    public List<McpExchange> exchanges() {
        return List.copyOf(exchanges);
    }

    public int size() {
        return exchanges.size();
    }

    public void clear() {
        exchanges.clear();
    }

    /** Short label from the JSON-RPC payload, e.g. "tools/call get_results". */
    private String summarize(String body) {
        String trimmed = truncate(body);
        if (trimmed.isBlank()) {
            return "(empty)";
        }
        try {
            JsonNode node = mapper.readTree(trimmed);
            List<String> labels = new ArrayList<>();
            for (JsonNode message : node.isArray() ? node : List.of(node)) {
                String method = message.path("method").asText("");
                if (method.isEmpty()) {
                    labels.add(message.has("result") || message.has("error") ? "response" : "?");
                }
                else if (method.equals("tools/call")) {
                    labels.add(method + " " + message.path("params").path("name").asText(""));
                }
                else {
                    labels.add(method);
                }
            }
            return String.join(", ", labels);
        }
        catch (Exception e) {
            return "(non-JSON)";
        }
    }

    /** One-line rendering: JSON compacted, SSE frames reduced to their data payloads. */
    private String compact(String body) {
        String trimmed = truncate(body);
        if (trimmed.isBlank()) {
            return "";
        }
        List<String> payloads = payloadsOf(trimmed);
        List<String> lines = new ArrayList<>();
        for (String payload : payloads) {
            try {
                lines.add(mapper.writeValueAsString(mapper.readTree(payload)));
            }
            catch (Exception e) {
                lines.add(payload.replaceAll("\\s+", " ").trim());
            }
        }
        return String.join(" | ", lines);
    }

    /** Multi-line pretty rendering of the same payloads. */
    private String pretty(String body) {
        String trimmed = truncate(body);
        if (trimmed.isBlank()) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        for (String payload : payloadsOf(trimmed)) {
            try {
                JsonNode tree = embedNestedJson(mapper.readTree(payload));
                lines.add(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree));
            }
            catch (Exception e) {
                lines.add(payload);
            }
        }
        return String.join("\n\n", lines);
    }

    /**
     * Recursively replaces string values that contain JSON (e.g. the
     * {@code result.content[].text} field of a tools/call response) with the
     * parsed JSON itself, so the expanded view shows it unescaped and
     * pretty-printed instead of as one escaped string.
     */
    private JsonNode embedNestedJson(JsonNode node) {
        if (node.isTextual()) {
            String text = node.asText().trim();
            boolean looksLikeJson = (text.startsWith("{") && text.endsWith("}"))
                    || (text.startsWith("[") && text.endsWith("]"));
            if (looksLikeJson) {
                try {
                    return embedNestedJson(mapper.readTree(text));
                }
                catch (Exception notJson) {
                    return node;
                }
            }
            return node;
        }
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            object.fieldNames().forEachRemaining(field -> object.set(field, embedNestedJson(object.get(field))));
        }
        else if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            for (int i = 0; i < array.size(); i++) {
                array.set(i, embedNestedJson(array.get(i)));
            }
        }
        return node;
    }

    /** Extracts the data payloads of an SSE stream, or returns the body as a single payload. */
    private static List<String> payloadsOf(String body) {
        if (!body.lines().anyMatch(line -> line.startsWith("data:"))) {
            return List.of(body);
        }
        List<String> payloads = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : body.lines().toList()) {
            if (line.startsWith("data:")) {
                current.append(line.substring(5).stripLeading());
            }
            else if (line.isBlank() && !current.isEmpty()) {
                payloads.add(current.toString());
                current.setLength(0);
            }
        }
        if (!current.isEmpty()) {
            payloads.add(current.toString());
        }
        return payloads;
    }

    private static String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > MAX_BODY_CHARS ? body.substring(0, MAX_BODY_CHARS) + " …(truncated)" : body;
    }

    public record McpExchange(
            long id,
            LocalDateTime timestamp,
            String httpMethod,
            String path,
            int status,
            long durationMs,
            String sessionId,
            String summary,
            String requestCompact,
            String requestPretty,
            String responseCompact,
            String responsePretty) {

        public String shortSession() {
            return sessionId.length() > 8 ? sessionId.substring(0, 8) : sessionId;
        }
    }
}
