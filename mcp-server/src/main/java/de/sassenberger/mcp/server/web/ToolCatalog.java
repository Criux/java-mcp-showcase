package de.sassenberger.mcp.server.web;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.stereotype.Component;

/**
 * Read-only catalog of the {@link McpTool @McpTool} methods registered in this
 * application, used by the management UI to display what the MCP server offers.
 */
@Component
public class ToolCatalog {

    private final ApplicationContext applicationContext;
    private final List<ToolInfo> tools = new ArrayList<>();

    public ToolCatalog(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    void scan() {
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> type = applicationContext.getType(beanName, false);
            if (type == null) {
                continue;
            }
            Class<?> targetClass = ClassUtils.getUserClass(type);
            for (Method method : targetClass.getDeclaredMethods()) {
                McpTool annotation = method.getAnnotation(McpTool.class);
                if (annotation == null) {
                    continue;
                }
                List<ParamInfo> params = new ArrayList<>();
                for (Parameter parameter : method.getParameters()) {
                    McpToolParam paramAnnotation = parameter.getAnnotation(McpToolParam.class);
                    params.add(new ParamInfo(
                            parameter.getName(),
                            parameter.getType().getSimpleName(),
                            paramAnnotation != null && paramAnnotation.required(),
                            paramAnnotation == null ? "" : paramAnnotation.description()));
                }
                tools.add(new ToolInfo(
                        annotation.name().isEmpty() ? method.getName() : annotation.name(),
                        annotation.description(),
                        targetClass.getSimpleName(),
                        method.getReturnType().getSimpleName(),
                        annotation.annotations().readOnlyHint(),
                        params));
            }
        }
        tools.sort(Comparator.comparing(ToolInfo::name));
    }

    public List<ToolInfo> tools() {
        return tools;
    }

    public record ToolInfo(
            String name,
            String description,
            String beanClass,
            String returnType,
            boolean readOnly,
            List<ParamInfo> params) {
    }

    public record ParamInfo(
            String name,
            String type,
            boolean required,
            String description) {
    }
}
