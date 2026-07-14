package de.sassenberger.mcp.server.results;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Inventory of the bundled result snapshots, stored as
 * {@code data/results/index.json}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResultsIndex(
        String note,
        List<YearEntry> years) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record YearEntry(
            int year,
            String sourceUrl,
            List<EventEntry> events) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventEntry(
            String code,
            String name,
            int resultCount) {
    }
}
