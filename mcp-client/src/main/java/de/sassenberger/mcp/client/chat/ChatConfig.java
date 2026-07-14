package de.sassenberger.mcp.client.chat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    private static final Logger log = LoggerFactory.getLogger(ChatConfig.class);

    /** Used if the support contact cannot be fetched from the MCP server at startup. */
    private static final String FALLBACK_EMAIL = "info@vfl-sassenberg.de";
    private static final String FALLBACK_PHONE = "+49 2583 91 92 15";

    /**
     * Strict grounding policy: the assistant may only use MCP tool results,
     * never its own training knowledge, and answers everything it cannot
     * ground with a standardized support-contact fallback.
     */
    private static final String SYSTEM_PROMPT = """
            You are the official assistant of the Sassenberger Triathlon, a triathlon event
            at the Feldmarksee in Sassenberg, Germany. You answer in the language the user
            writes in.

            STRICT GROUNDING RULES — these override everything else:

            1. The ONLY permitted source of factual information are the MCP tools available
               to you (event info, races, registration, schedule, courses, past results).
               Before answering any factual question, call the appropriate tool(s) and base
               your answer exclusively on what they return.
            2. NEVER answer from your own general or training knowledge. Do not guess,
               estimate, extrapolate or invent names, numbers, dates, prices, rules or
               results — not even when you are confident. If a tool did not return it,
               you do not know it.
            3. You have NO internet access and cannot look anything up online. If asked to
               browse, fetch a URL or research something, state that you cannot.
            4. If the tools do not provide the requested information, or the question is
               not about the Sassenberger Triathlon, reply (in the user's language) that
               this information is not available to you, and refer the user to the
               support contact as the alternative:
               - E-Mail: %s
               - Telefon: %s
               You may also mention the official website https://sassenbergertriathlon.de.
            5. Greetings, thanks and simple conversational politeness may be answered
               directly, but must not contain any factual claims.

            When reporting results, mention the year and race, and remember that 'place' is
            the position within the athlete's gender, so a race has both a male and a female
            winner with place 1.
            """;

    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(30)
                .build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ToolCallbackProvider mcpTools,
            ChatMemory chatMemory, List<McpSyncClient> mcpClients) {
        ToolCallback[] recordingCallbacks = Arrays.stream(mcpTools.getToolCallbacks())
                .map(RecordingToolCallback::new)
                .toArray(ToolCallback[]::new);
        log.info("Wired {} MCP tools into the ChatClient: {}", recordingCallbacks.length,
                Arrays.stream(recordingCallbacks).map(tc -> tc.getToolDefinition().name()).toList());
        return builder
                .defaultSystem(SYSTEM_PROMPT.formatted(supportContact(mcpClients)))
                .defaultToolCallbacks(recordingCallbacks)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * Fetches the organizer's support contact from the MCP server's
     * {@code get_event_info} tool so the fallback message always matches the
     * server's curated data; falls back to known constants if that fails.
     */
    private Object[] supportContact(List<McpSyncClient> mcpClients) {
        try {
            McpSchema.CallToolResult result = mcpClients.get(0)
                    .callTool(new McpSchema.CallToolRequest("get_event_info", Map.of()));
            String text = result.content().stream()
                    .filter(McpSchema.TextContent.class::isInstance)
                    .map(content -> ((McpSchema.TextContent) content).text())
                    .findFirst()
                    .orElseThrow();
            JsonNode organizer = new ObjectMapper().readTree(text).path("organizer");
            String email = organizer.path("email").asText(FALLBACK_EMAIL);
            String phone = organizer.path("phone").asText(FALLBACK_PHONE);
            log.info("Support contact loaded from MCP server: {} / {}", email, phone);
            return new Object[] { email, phone };
        }
        catch (Exception e) {
            log.warn("Could not fetch support contact from the MCP server, using fallback: {}",
                    e.getMessage());
            return new Object[] { FALLBACK_EMAIL, FALLBACK_PHONE };
        }
    }
}
