package de.sassenberger.mcp.server.tools;

import java.util.List;
import java.util.stream.Collectors;

import de.sassenberger.mcp.server.results.EventResults;
import de.sassenberger.mcp.server.results.ResultRow;
import de.sassenberger.mcp.server.results.ResultsIndex;
import de.sassenberger.mcp.server.results.ResultsService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP tools over the bundled past-results snapshots (mika:timing, 2016-2025;
 * 2020/2021 were cancelled because of the Corona pandemic).
 */
@Component
public class ResultsTools {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;
    private static final int MAX_SEARCH_MATCHES = 50;

    private final ResultsService results;

    public ResultsTools(ResultsService results) {
        this.results = results;
    }

    @McpTool(name = "list_result_editions",
            description = "Lists all editions (years) of the Sassenberger Triathlon with available past "
                    + "results and, per year, the races (event codes and names) that were held. Use this "
                    + "first to find the correct year and event code for get_results. Results exist for "
                    + "2016-2019 and 2022-2025; the 2020 and 2021 editions were cancelled (Corona).",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public ResultsIndex listResultEditions() {
        return results.index();
    }

    @McpTool(name = "get_results",
            description = "Past results of one race of the Sassenberger Triathlon in a given year, "
                    + "ordered by finish position, with swim/bike/run splits and finish time. NOTE: "
                    + "'place' is the position within the athlete's gender (mika:timing convention), so "
                    + "there is one place-1 man AND one place-1 woman; 'agePlace' is the position within "
                    + "the age group. Get year and event code from list_result_editions.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public ResultsPage getResults(
            @McpToolParam(description = "Edition year, e.g. 2025", required = true) int year,
            @McpToolParam(description = "Event code from list_result_editions, e.g. MD (middle distance), "
                    + "OL (olympic), VD (Volksdistanz), MGP (Münsterland GrandPrix), F3R (relay)",
                    required = true) String eventCode,
            @McpToolParam(description = "Maximum number of rows to return, default 10, max 100",
                    required = false) Integer limit) {
        EventResults event = results.results(year, eventCode).orElseThrow(() -> {
            if (year == 2020 || year == 2021) {
                return new IllegalArgumentException("The " + year + " edition was cancelled because of "
                        + "the Corona pandemic; no results exist.");
            }
            String available = results.index().years().stream()
                    .filter(y -> y.year() == year)
                    .flatMap(y -> y.events().stream())
                    .map(e -> e.code() + " (" + e.name() + ")")
                    .collect(Collectors.joining(", "));
            return new IllegalArgumentException(available.isEmpty()
                    ? "No results for year " + year + ". Use list_result_editions to see available years."
                    : "No event '" + eventCode + "' in " + year + ". Available: " + available);
        });

        int effectiveLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        List<ResultRow> rows = event.results().stream().limit(effectiveLimit).toList();
        return new ResultsPage(event.year(), event.eventCode(), event.eventName(),
                event.resultCount(), rows.size(), rows);
    }

    @McpTool(name = "search_athlete",
            description = "Searches all bundled past results of the Sassenberger Triathlon (2016-2025) "
                    + "for an athlete or relay team by name (case-insensitive substring match, format "
                    + "'Lastname, Firstname'). Returns every matching finish with year, race, splits and "
                    + "finish time, newest first.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public AthleteSearchResult searchAthlete(
            @McpToolParam(description = "Full or partial name, e.g. 'Rechter' or 'Rechter, Insa'",
                    required = true) String name,
            @McpToolParam(description = "Restrict the search to one edition year, e.g. 2025",
                    required = false) Integer year) {
        List<ResultsService.AthleteMatch> matches = results.searchAthlete(name, year);
        boolean truncated = matches.size() > MAX_SEARCH_MATCHES;
        return new AthleteSearchResult(matches.size(), truncated,
                truncated ? matches.subList(0, MAX_SEARCH_MATCHES) : matches);
    }

    public record ResultsPage(
            int year,
            String eventCode,
            String eventName,
            int totalResults,
            int returnedResults,
            List<ResultRow> results) {
    }

    public record AthleteSearchResult(
            int totalMatches,
            boolean truncated,
            List<ResultsService.AthleteMatch> matches) {
    }
}
