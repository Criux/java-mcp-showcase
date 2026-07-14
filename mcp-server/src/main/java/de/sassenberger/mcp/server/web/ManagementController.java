package de.sassenberger.mcp.server.web;

import de.sassenberger.mcp.server.content.TriathlonContentService;
import de.sassenberger.mcp.server.results.ResultsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Read-only management interface of the MCP server (FreeMarker).
 */
@Controller
public class ManagementController {

    private final ToolCatalog toolCatalog;
    private final TriathlonContentService content;
    private final ResultsService results;

    @Value("${spring.ai.mcp.server.name}")
    private String serverName;

    @Value("${spring.ai.mcp.server.version}")
    private String serverVersion;

    @Value("${spring.ai.mcp.server.protocol}")
    private String protocol;

    public ManagementController(ToolCatalog toolCatalog, TriathlonContentService content,
            ResultsService results) {
        this.toolCatalog = toolCatalog;
        this.content = content;
        this.results = results;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        common(model);
        model.addAttribute("event", content.event());
        model.addAttribute("registration", content.registration());
        model.addAttribute("raceCount", content.races().size());
        model.addAttribute("yearCount", results.index().years().size());
        model.addAttribute("totalResults", results.totalResultCount());
        return "dashboard";
    }

    @GetMapping("/tools")
    public String tools(Model model) {
        common(model);
        return "tools";
    }

    @GetMapping("/data")
    public String data(Model model) {
        common(model);
        model.addAttribute("races", content.races());
        model.addAttribute("registration", content.registration());
        model.addAttribute("schedule", content.schedule());
        model.addAttribute("index", results.index());
        model.addAttribute("totalResults", results.totalResultCount());
        return "data";
    }

    private void common(Model model) {
        model.addAttribute("serverName", serverName);
        model.addAttribute("serverVersion", serverVersion);
        model.addAttribute("protocol", protocol);
        model.addAttribute("mcpEndpoint", "/mcp");
        model.addAttribute("tools", toolCatalog.tools());
    }
}
