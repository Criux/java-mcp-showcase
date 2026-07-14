package de.sassenberger.mcp.client.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Minimal chat UI (FreeMarker + classic form POST with redirect-after-post).
 * The conversation history shown on the page lives in the HTTP session; the
 * LLM-side memory is handled by the {@code MessageChatMemoryAdvisor} keyed by
 * a per-session conversation id.
 */
@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    static final String HISTORY_ATTRIBUTE = "chatHistory";
    static final String CONVERSATION_ATTRIBUTE = "conversationId";

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/")
    public String chat(Model model, HttpSession session) {
        model.addAttribute("history", history(session));
        return "chat";
    }

    /** No-JavaScript fallback: classic form POST with redirect-after-post. */
    @PostMapping("/chat")
    public String ask(@RequestParam("message") String message, HttpSession session) {
        String trimmed = message == null ? "" : message.strip();
        if (!trimmed.isEmpty()) {
            exchange(trimmed, session);
        }
        return "redirect:/";
    }

    /** AJAX endpoint used by the chat page: returns the assistant reply as JSON. */
    @PostMapping("/api/chat")
    @ResponseBody
    public ChatEntry askJson(@RequestParam("message") String message, HttpSession session) {
        String trimmed = message == null ? "" : message.strip();
        if (trimmed.isEmpty()) {
            return new ChatEntry("error", "Empty message.", List.of());
        }
        return exchange(trimmed, session);
    }

    /** Runs one chat round trip and records both sides in the session history. */
    private ChatEntry exchange(String message, HttpSession session) {
        List<ChatEntry> history = history(session);
        history.add(new ChatEntry("user", message, List.of()));

        RecordingToolCallback.drain();
        ChatEntry reply;
        try {
            String answer = chatClient.prompt()
                    .user(message)
                    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId(session)))
                    .call()
                    .content();
            reply = new ChatEntry("assistant", answer, RecordingToolCallback.drain());
        }
        catch (Exception e) {
            log.error("Chat request failed", e);
            reply = new ChatEntry("error",
                    "The request failed: " + e.getMessage()
                            + " — is the MCP server running and the OpenAI API key valid?",
                    RecordingToolCallback.drain());
        }
        history.add(reply);
        return reply;
    }

    @PostMapping("/reset")
    public String reset(HttpSession session) {
        session.removeAttribute(HISTORY_ATTRIBUTE);
        session.removeAttribute(CONVERSATION_ATTRIBUTE);
        return "redirect:/";
    }

    @SuppressWarnings("unchecked")
    private List<ChatEntry> history(HttpSession session) {
        List<ChatEntry> history = (List<ChatEntry>) session.getAttribute(HISTORY_ATTRIBUTE);
        if (history == null) {
            history = new ArrayList<>();
            session.setAttribute(HISTORY_ATTRIBUTE, history);
        }
        return history;
    }

    private String conversationId(HttpSession session) {
        String conversationId = (String) session.getAttribute(CONVERSATION_ATTRIBUTE);
        if (conversationId == null) {
            conversationId = UUID.randomUUID().toString();
            session.setAttribute(CONVERSATION_ATTRIBUTE, conversationId);
        }
        return conversationId;
    }

    public record ChatEntry(String role, String text, List<String> toolsUsed) {
    }
}
