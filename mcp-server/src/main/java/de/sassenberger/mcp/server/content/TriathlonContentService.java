package de.sassenberger.mcp.server.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Loads and serves the curated triathlon content shipped with the server.
 */
@Service
public class TriathlonContentService {

    private final TriathlonContent content;

    public TriathlonContentService() {
        ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        try (InputStream in = new ClassPathResource("data/triathlon-content.yaml").getInputStream()) {
            this.content = yaml.readValue(in, TriathlonContent.class);
        }
        catch (IOException e) {
            throw new UncheckedIOException("Failed to load data/triathlon-content.yaml", e);
        }
    }

    public TriathlonContent.EventInfo event() {
        return content.event();
    }

    public List<TriathlonContent.Race> races() {
        return content.races();
    }

    public Optional<TriathlonContent.Race> race(String raceId) {
        return content.races().stream()
                .filter(r -> r.id().equalsIgnoreCase(raceId))
                .findFirst();
    }

    public TriathlonContent.Registration registration() {
        return content.registration();
    }

    public List<TriathlonContent.ScheduleDay> schedule() {
        return content.schedule();
    }
}
