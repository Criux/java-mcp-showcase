package de.sassenberger.mcp.client.chat;

import java.util.Arrays;

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

    private static final String SYSTEM_PROMPT = """
            You are the friendly assistant of the Sassenberger Triathlon, a triathlon event
            at the Feldmarksee in Sassenberg, Germany (next edition: Sunday 2026-08-02).

            Answer questions about the event, the race distances, registration, the schedule,
            the courses and past results. Always use the available MCP tools to look up facts
            before answering — do not invent details. The tool data is partly in German;
            answer in the language the user writes in.

            When reporting results, mention the year and race, and remember that 'place' is
            the position within the athlete's gender, so a race has both a male and a female
            winner with place 1. If something is not covered by the tools, say so and point
            to the official website https://sassenbergertriathlon.de.
            """;

    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(30)
                .build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ToolCallbackProvider mcpTools, ChatMemory chatMemory) {
        ToolCallback[] recordingCallbacks = Arrays.stream(mcpTools.getToolCallbacks())
                .map(RecordingToolCallback::new)
                .toArray(ToolCallback[]::new);
        log.info("Wired {} MCP tools into the ChatClient: {}", recordingCallbacks.length,
                Arrays.stream(recordingCallbacks).map(tc -> tc.getToolDefinition().name()).toList());
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(recordingCallbacks)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
