package de.sassenberger.mcp.server.results;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Snapshot of all results of one event (race) in one edition/year,
 * stored as {@code data/results/{year}/{eventCode}.json}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventResults(
        int year,
        String eventCode,
        String eventName,
        String sourceUrl,
        int resultCount,
        List<ResultRow> results) {
}
