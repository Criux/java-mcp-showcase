package de.sassenberger.mcp.server.results;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One finisher row from a mika:timing result list.
 *
 * <p>Note: {@code place} and {@code agePlace} follow the mika:timing
 * convention — place within the athlete's gender and age group respectively,
 * not one combined ranking. Relay teams carry the team name in {@code name}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResultRow(
        Integer place,
        Integer agePlace,
        String name,
        String nation,
        String bib,
        String ageGroup,
        String club,
        String swim,
        String bike,
        String run,
        String finish) {
}
