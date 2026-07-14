# MCP Showcase — Sassenberger Triathlon

A Model Context Protocol (MCP) showcase in Java: two independent Spring Boot
applications built with the [Spring AI](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html)
MCP starters.

- **`mcp-server/`** exposes MCP **tools** that answer questions about the
  [Sassenberger Triathlon](https://sassenbergertriathlon.de/de/home) (event
  info, races/distances, registration, schedule, past results 2016–2025) over
  the MCP **Streamable HTTP** transport, plus a read-only management interface
  built with Apache FreeMarker.
- **`mcp-client/`** is a chat web app (FreeMarker UI) whose LLM (OpenAI)
  answers user questions by calling the server's MCP tools.

```
                 ┌─────────────────────────────┐        ┌──────────────────────────────┐
   Browser ───►  │  mcp-client  :8081          │        │  mcp-server  :8080           │
   (chat UI)     │  Spring Boot + FreeMarker   │  MCP   │  Spring Boot + FreeMarker    │
                 │  ChatClient (OpenAI)        │ ─────► │  8 @McpTool tools            │
                 │  spring-ai-starter-         │  HTTP  │  spring-ai-starter-          │
                 │    mcp-client               │  /mcp  │    mcp-server-webmvc         │
                 └─────────────────────────────┘        │  management UI on /          │
                                                        │  data: curated YAML +        │
                                                        │  crawled result snapshots    │
                                                        └──────────────────────────────┘
```

Stack: Java 17+, Spring Boot 4.1, Spring AI 2.0.0 (MCP Java SDK 2.0),
Maven, Apache FreeMarker, Jsoup (offline crawler only).

## MCP tools offered by the server

| Tool | Parameters | Answers |
|---|---|---|
| `get_event_info` | – | What/where/when, venue, organizer, contact, rules |
| `list_races` | – | All distances with fees, eligibility, time limits |
| `get_race_details` | `raceId` | One race incl. course description and link |
| `get_registration_info` | – | How to register, window, sold-out status, policies |
| `get_schedule` | – | Race-weekend timeline |
| `list_result_editions` | – | Years and races with available past results |
| `get_results` | `year`, `eventCode`, `limit?` | Finishers with splits, ordered by place |
| `search_athlete` | `name`, `year?` | All finishes of an athlete/team across years |

Data sources:

- **Curated content** (`mcp-server/src/main/resources/data/triathlon-content.yaml`):
  event facts transcribed by hand from the website, because distances, fees and
  the schedule are published there only as images/PDFs. Refresh once per season.
- **Result snapshots** (`mcp-server/src/main/resources/data/results/`): crawled
  from the public mika:timing result pages (9,617 finisher rows, 2016–2019 and
  2022–2025; the 2020/2021 editions were cancelled). `place` is the position
  within the athlete's gender — mika:timing convention.

## Running

Requirements: JDK 17+ (tested with 25), Maven 3.9+, an OpenAI API key for the client.

**1. Start the server** (port 8080):

```bash
cd mcp-server
mvn spring-boot:run
```

- Management UI: <http://localhost:8080/> (dashboard, tool catalog, data overview,
  MCP traffic history)
- MCP endpoint: `POST http://localhost:8080/mcp` (Streamable HTTP, JSON-RPC)

**2. Start the client** (port 8081):

```bash
cd mcp-client
export OPENAI_API_KEY=sk-...
mvn spring-boot:run
```

- Chat UI: <http://localhost:8081/> — each answer shows which MCP tools were used.
- `MCP_SERVER_URL` overrides the server address (default `http://localhost:8080`).

Example questions:

- *Which distances can I race in Sassenberg?*
- *Wie melde ich mich an — gibt es noch freie Plätze?* (→ sold out)
- *Who won the Mitteldistanz in 2025?*
- *Were there results in 2020?* (→ Corona cancellation)

## MCP traffic history

The **History** tab of the management UI (<http://localhost:8080/history>)
records every message exchanged between MCP clients and the server: a servlet
filter on `/mcp` captures request and response bodies (SSE frames are unwrapped
to their JSON payloads) into an in-memory log of the last 500 exchanges. Each
entry shows time, HTTP method/status, duration, MCP session and the JSON-RPC
method; payloads are collapsed to a single line and expand on click into
pretty-printed JSON. Long-lived GET listening streams are logged without body
capture so streaming is not affected.

## Refreshing the result snapshots

The snapshots are committed, so this is only needed for a new season:

```bash
cd mcp-server
mvn -q compile exec:java -Dexec.mainClass=de.sassenberger.mcp.server.ingest.ResultsCrawler
```

The crawler discovers each year's races from the mika:timing landing pages,
pages through the result lists (browser User-Agent required — mika:timing
returns 403 otherwise) and rewrites `data/results/`. Add new years to
`ResultsCrawler.sources()`.

## Verifying the MCP endpoint by hand

```bash
# initialize (note the returned Mcp-Session-Id header)
curl -i -X POST http://localhost:8080/mcp \
  -H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream' \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"curl","version":"0"}}}'

# list tools (replace <SID> with the Mcp-Session-Id from above)
curl -s -X POST http://localhost:8080/mcp \
  -H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream' \
  -H 'Mcp-Session-Id: <SID>' \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}'
```

## Notes

- The original brief asked for SSE; this showcase uses **Streamable HTTP**
  (`spring.ai.mcp.server.protocol=STREAMABLE`) because SSE is deprecated in
  Spring AI 2.0 / current MCP spec. Switching back is a one-line config change
  (`protocol: SSE`, client side `spring.ai.mcp.client.sse.connections...`).
- All facts served by the tools are in German (as published); the client's
  system prompt tells the LLM to answer in the user's language.
