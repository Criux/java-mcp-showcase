package de.sassenberger.mcp.server.tools;

import java.util.List;
import java.util.stream.Collectors;

import de.sassenberger.mcp.server.content.TriathlonContent;
import de.sassenberger.mcp.server.content.TriathlonContentService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

/**
 * MCP tools answering questions about the event itself: general information,
 * races/distances, registration and the race-weekend schedule. Backed by the
 * curated content in {@code data/triathlon-content.yaml}.
 */
@Component
public class TriathlonInfoTools {

    private final TriathlonContentService content;

    public TriathlonInfoTools(TriathlonContentService content) {
        this.content = content;
    }

    @McpTool(name = "get_event_info",
            description = "General information about the Sassenberger Triathlon: what, where and when "
                    + "the event takes place, venue, organizer with contact details, history and "
                    + "competition rules. Facts are in German.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public TriathlonContent.EventInfo getEventInfo() {
        return content.event();
    }

    @McpTool(name = "list_races",
            description = "Lists all races (distances) of the Sassenberger Triathlon with swim/bike/run "
                    + "distances in km, entry fee in EUR, eligibility, license requirements and time "
                    + "limits: Volksdistanz, StaffelCup relay, olympic/short distance, military cup, "
                    + "Münsterland GrandPrix, middle distance and league races.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public List<TriathlonContent.Race> listRaces() {
        return content.races();
    }

    @McpTool(name = "get_race_details",
            description = "Details of a single race of the Sassenberger Triathlon including the course "
                    + "description and a link to course information. Use list_races first to discover "
                    + "the race ids.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public TriathlonContent.Race getRaceDetails(
            @org.springframework.ai.mcp.annotation.McpToolParam(
                    description = "Race id, e.g. volksdistanz, staffelcup, kurzdistanz, militaer-cup, "
                            + "grandprix, mitteldistanz, nrwtv-ligen",
                    required = true) String raceId) {
        return content.race(raceId).orElseThrow(() -> new IllegalArgumentException(
                "Unknown race id '" + raceId + "'. Valid ids: "
                        + content.races().stream().map(TriathlonContent.Race::id)
                                .collect(Collectors.joining(", "))));
    }

    @McpTool(name = "get_registration_info",
            description = "How to register for the Sassenberger Triathlon: registration platform and "
                    + "links, registration window, current availability status (sold out or not), "
                    + "fees policy and what the entry fee includes.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public TriathlonContent.Registration getRegistrationInfo() {
        return content.registration();
    }

    @McpTool(name = "get_schedule",
            description = "The race-weekend schedule of the Sassenberger Triathlon (accreditation, "
                    + "transition zone, starts, bike checkout, event end).",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public List<TriathlonContent.ScheduleDay> getSchedule() {
        return content.schedule();
    }
}
