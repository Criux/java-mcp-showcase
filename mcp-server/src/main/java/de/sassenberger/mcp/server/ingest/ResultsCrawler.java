package de.sassenberger.mcp.server.ingest;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.sassenberger.mcp.server.results.EventResults;
import de.sassenberger.mcp.server.results.ResultRow;
import de.sassenberger.mcp.server.results.ResultsIndex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Offline ingest utility: crawls the public mika:timing result pages of the
 * Sassenberger Triathlon and writes JSON snapshots that are bundled with the
 * MCP server. Not part of the Spring Boot application — run manually:
 *
 * <pre>
 * mvn -q compile exec:java -Dexec.mainClass=de.sassenberger.mcp.server.ingest.ResultsCrawler
 * </pre>
 *
 * mika:timing rejects non-browser user agents with HTTP 403, hence the
 * browser User-Agent below. 2020 and 2021 have no results (editions cancelled
 * because of the Corona pandemic).
 */
public final class ResultsCrawler {

    private static final String USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 50;
    private static final long DELAY_MS = 300;

    private static final Pattern NAME_NATION = Pattern.compile("^(.*)\\s+\\(([A-Z]{3})\\)$");

    /** year -> result-service base URL (host moved from .net to .com in 2022). */
    private static Map<Integer, String> sources() {
        Map<Integer, String> sources = new LinkedHashMap<>();
        for (int year = 2016; year <= 2019; year++) {
            sources.put(year, "https://sassenbergertriathlon.r.mikatiming.net/" + year + "/");
        }
        for (int year = 2022; year <= 2025; year++) {
            sources.put(year, "https://sassenbergertriathlon.r.mikatiming.com/" + year + "/");
        }
        return sources;
    }

    public static void main(String[] args) throws Exception {
        File outDir = new File(args.length > 0 ? args[0] : "src/main/resources/data/results");
        outDir.mkdirs();

        ObjectMapper json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        List<ResultsIndex.YearEntry> yearEntries = new ArrayList<>();

        for (Map.Entry<Integer, String> source : sources().entrySet()) {
            int year = source.getKey();
            String baseUrl = source.getValue();
            System.out.printf("== %d (%s)%n", year, baseUrl);

            Map<String, String> events;
            try {
                events = discoverEvents(baseUrl);
            }
            catch (Exception e) {
                System.out.printf("   SKIPPING year %d: %s%n", year, e);
                continue;
            }

            File yearDir = new File(outDir, String.valueOf(year));
            yearDir.mkdirs();
            List<ResultsIndex.EventEntry> eventEntries = new ArrayList<>();

            for (Map.Entry<String, String> event : events.entrySet()) {
                String code = event.getKey();
                String name = event.getValue();
                try {
                    List<ResultRow> rows = crawlEvent(baseUrl, code);
                    if (rows.isEmpty()) {
                        System.out.printf("   %-6s %-45s -> no rows, skipped%n", code, name);
                        continue;
                    }
                    EventResults results = new EventResults(year, code, name, baseUrl, rows.size(), rows);
                    json.writeValue(new File(yearDir, code + ".json"), results);
                    eventEntries.add(new ResultsIndex.EventEntry(code, name, rows.size()));
                    System.out.printf("   %-6s %-45s -> %d results%n", code, name, rows.size());
                }
                catch (Exception e) {
                    System.out.printf("   %-6s %-45s -> FAILED: %s%n", code, name, e);
                }
            }
            yearEntries.add(new ResultsIndex.YearEntry(year, baseUrl, eventEntries));
        }

        ResultsIndex index = new ResultsIndex(
                "Results of the Sassenberger Triathlon crawled from mika:timing. "
                        + "2020 and 2021 are missing because those editions were cancelled (Corona pandemic). "
                        + "Places are per gender (mika:timing convention).",
                yearEntries);
        json.writeValue(new File(outDir, "index.json"), index);
        System.out.println("Wrote " + new File(outDir, "index.json").getAbsolutePath());
    }

    /** Reads the event dropdown of the year's landing page: code -> label. */
    private static Map<String, String> discoverEvents(String baseUrl) throws Exception {
        Document doc = fetch(baseUrl);
        Map<String, String> events = new LinkedHashMap<>();
        for (Element option : doc.select("select[name=event] option")) {
            String code = option.attr("value").trim();
            if (!code.isEmpty()) {
                events.putIfAbsent(code, option.text().trim());
            }
        }
        // Some skins render the dropdown without a name attribute; fall back to
        // any option whose value looks like an event code next to a "search_event" form.
        if (events.isEmpty()) {
            for (Element option : doc.select("option[value~=^[A-Z0-9]{2,6}$]")) {
                events.putIfAbsent(option.attr("value").trim(), option.text().trim());
            }
        }
        return events;
    }

    private static List<ResultRow> crawlEvent(String baseUrl, String eventCode) throws Exception {
        List<ResultRow> rows = new ArrayList<>();
        for (int page = 1; page <= MAX_PAGES; page++) {
            String url = baseUrl + "?pid=list&event=" + eventCode + "&num_results=" + PAGE_SIZE + "&page=" + page;
            Document doc = fetch(url);
            List<Element> items = doc.select("li.list-group-item.row").stream()
                    .filter(li -> !li.hasClass("list-group-header"))
                    .toList();
            if (!items.isEmpty()) {
                for (Element item : items) {
                    rows.add(parseRow(item));
                }
            }
            else {
                // Older editions (2016/2017) render a plain table instead of a list group.
                items = doc.select("table.list-table tr").stream()
                        .filter(tr -> !tr.hasClass("list-head") && tr.select("th").isEmpty()
                                && tr.select("td").size() >= 10)
                        .toList();
                for (Element item : items) {
                    rows.add(parseTableRow(item));
                }
            }
            if (items.size() < PAGE_SIZE) {
                break;
            }
        }
        return rows;
    }

    private static ResultRow parseRow(Element item) {
        Integer place = parseInt(text(item, ".type-place.place-primary"));
        Integer agePlace = parseInt(text(item, ".type-place.place-secondary"));

        String fullName = text(item, ".type-fullname");
        String name = fullName;
        String nation = null;
        if (fullName != null) {
            Matcher m = NAME_NATION.matcher(fullName);
            if (m.matches()) {
                name = m.group(1).trim();
                nation = m.group(2);
            }
        }

        // Generic fields carry their column label in a (mobile-only) child div;
        // the value is the element's own text.
        String bib = null;
        String club = null;
        for (Element field : item.select(".type-field")) {
            String label = field.select(".list-label").text();
            String value = clean(field.ownText());
            if (label.startsWith("Startnr")) {
                bib = value;
            }
            else if (label.startsWith("Verein")) {
                club = value;
            }
        }
        String ageGroup = clean(item.select(".type-age_class").isEmpty()
                ? null : item.select(".type-age_class").first().ownText());

        String swim = null;
        String bike = null;
        String run = null;
        String finish = null;
        for (Element time : item.select(".type-time")) {
            String label = time.select(".list-label").text();
            String value = clean(time.ownText());
            switch (label) {
                case "SWIM" -> swim = value;
                case "BIKE" -> bike = value;
                case "RUN" -> run = value;
                case "Ziel", "Finish" -> finish = value;
                default -> {
                    if (finish == null) {
                        finish = value;
                    }
                }
            }
        }
        return new ResultRow(place, agePlace, name, nation, bib, ageGroup, club, swim, bike, run, finish);
    }

    /**
     * Parses one row of the 2016/2017 table layout. Column order:
     * place, place AK, bib, name (nation), AK, club, swim, bike, run, finish.
     */
    private static ResultRow parseTableRow(Element row) {
        List<Element> tds = row.select("td");
        String fullName = clean(tds.get(3).text().replaceFirst("^»\\s*", ""));
        String name = fullName;
        String nation = null;
        if (fullName != null) {
            Matcher m = NAME_NATION.matcher(fullName);
            if (m.matches()) {
                name = m.group(1).trim();
                nation = m.group(2);
            }
        }
        return new ResultRow(
                parseInt(clean(tds.get(0).text())),
                parseInt(clean(tds.get(1).text())),
                name,
                nation,
                clean(tds.get(2).text()),
                clean(tds.get(4).text()),
                clean(tds.get(5).text()),
                clean(tds.get(6).text()),
                clean(tds.get(7).text()),
                clean(tds.get(8).text()),
                clean(tds.get(9).text()));
    }

    private static Document fetch(String url) throws Exception {
        Thread.sleep(DELAY_MS);
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(20_000)
                .get();
    }

    private static String text(Element item, String selector) {
        Element el = item.selectFirst(selector);
        return el == null ? null : clean(el.text());
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replace('\u00A0', ' ').trim();
        return (cleaned.isEmpty() || cleaned.equals("–") || cleaned.equals("—") || cleaned.equals("-"))
                ? null : cleaned;
    }

    private static Integer parseInt(String value) {
        try {
            return value == null ? null : Integer.valueOf(value);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    private ResultsCrawler() {
    }
}
