package de.sassenberger.mcp.server.content;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Curated facts about the Sassenberger Triathlon, loaded from
 * {@code data/triathlon-content.yaml}. The figures are maintained by hand
 * because the website publishes them only as images/PDFs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TriathlonContent(
        EventInfo event,
        List<Race> races,
        Registration registration,
        List<ScheduleDay> schedule) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventInfo(
            String name,
            int edition,
            String date,
            String location,
            String venue,
            String description,
            int heldSince,
            String websiteUrl,
            Organizer organizer,
            List<String> rules) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Organizer(
            String name,
            String address,
            String chairperson,
            String phone,
            String email) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Race(
            String id,
            String name,
            double swimKm,
            double bikeKm,
            double runKm,
            Integer feeEur,
            String eligibility,
            String license,
            Integer timeLimitMinutes,
            String courseDescription,
            String courseInfoUrl) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Registration(
            String platform,
            String onlineUrl,
            String starterListUrl,
            String postalFormUrl,
            String infoUrl,
            String opens,
            String closes,
            String status,
            List<String> policies) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScheduleDay(
            String day,
            String date,
            List<ScheduleItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScheduleItem(
            String time,
            String activity) {
    }
}
