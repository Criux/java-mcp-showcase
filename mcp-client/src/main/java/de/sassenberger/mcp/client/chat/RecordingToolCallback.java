package de.sassenberger.mcp.client.chat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * Decorates an MCP tool callback and records every invocation, so the chat UI
 * can show which MCP tools were used to produce an answer. Works because sync
 * tool execution happens on the request thread.
 */
public class RecordingToolCallback implements ToolCallback {

    private static final ThreadLocal<List<String>> RECORDED = ThreadLocal.withInitial(ArrayList::new);

    private final ToolCallback delegate;

    public RecordingToolCallback(ToolCallback delegate) {
        this.delegate = delegate;
    }

    /** Returns and clears the tool names recorded on this thread. */
    public static List<String> drain() {
        List<String> calls = List.copyOf(RECORDED.get());
        RECORDED.get().clear();
        return calls;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        RECORDED.get().add(delegate.getToolDefinition().name());
        return delegate.call(toolInput);
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        RECORDED.get().add(delegate.getToolDefinition().name());
        return delegate.call(toolInput, toolContext);
    }
}
