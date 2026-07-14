package de.sassenberger.mcp.server.results;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

/**
 * Serves the bundled mika:timing result snapshots
 * ({@code data/results/index.json} + {@code data/results/{year}/{event}.json}),
 * generated offline by the {@code ResultsCrawler} ingest utility.
 */
@Service
public class ResultsService {

    private final ResultsIndex index;

    /** (year, eventCode) -> results, in index order. */
    private final Map<String, EventResults> resultsByYearAndEvent = new LinkedHashMap<>();

    public ResultsService() {
        // Own Jackson 2 mapper: the snapshots are written by the crawler with
        // Jackson 2, while Spring Boot 4 auto-configures only Jackson 3.
        ObjectMapper json = new ObjectMapper();
        var resolver = new PathMatchingResourcePatternResolver();
        try {
            this.index = json.readValue(
                    resolver.getResource("classpath:data/results/index.json").getInputStream(),
                    ResultsIndex.class);
            for (Resource resource : resolver.getResources("classpath:data/results/*/*.json")) {
                try (InputStream in = resource.getInputStream()) {
                    EventResults results = json.readValue(in, EventResults.class);
                    resultsByYearAndEvent.put(key(results.year(), results.eventCode()), results);
                }
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException("Failed to load result snapshots from data/results/", e);
        }
    }

    public ResultsIndex index() {
        return index;
    }

    public int totalResultCount() {
        return resultsByYearAndEvent.values().stream().mapToInt(EventResults::resultCount).sum();
    }

    public Optional<EventResults> results(int year, String eventCode) {
        return Optional.ofNullable(resultsByYearAndEvent.get(key(year, eventCode)));
    }

    /** Case-insensitive substring search over athlete/team names, optionally restricted to one year. */
    public List<AthleteMatch> searchAthlete(String name, Integer year) {
        String needle = name.toLowerCase(Locale.GERMAN);
        List<AthleteMatch> matches = new ArrayList<>();
        for (EventResults event : resultsByYearAndEvent.values()) {
            if (year != null && event.year() != year) {
                continue;
            }
            for (ResultRow row : event.results()) {
                if (row.name() != null && row.name().toLowerCase(Locale.GERMAN).contains(needle)) {
                    matches.add(new AthleteMatch(event.year(), event.eventCode(), event.eventName(), row));
                }
            }
        }
        matches.sort(Comparator.comparingInt(AthleteMatch::year).reversed());
        return matches;
    }

    public record AthleteMatch(int year, String eventCode, String eventName, ResultRow result) {
    }

    private static String key(int year, String eventCode) {
        return year + "/" + eventCode.toUpperCase(Locale.ROOT);
    }
}
