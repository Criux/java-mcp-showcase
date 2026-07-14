<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Sassenberger Triathlon · Assistant</title>
<style>
  :root { --accent: #0b6e4f; --accent-light: #e6f2ee; --border: #e2e2e2; --text: #222; --muted: #777; }
  * { box-sizing: border-box; }
  body { margin: 0; font-family: system-ui, -apple-system, "Segoe UI", sans-serif; color: var(--text); background: #fafafa; display: flex; flex-direction: column; min-height: 100vh; }
  header { background: var(--accent); color: #fff; padding: 0.9rem 1.5rem; display: flex; align-items: baseline; gap: 1rem; }
  header h1 { font-size: 1.1rem; margin: 0; font-weight: 600; }
  header span { opacity: 0.75; font-size: 0.85rem; }
  header form { margin-left: auto; }
  header button { background: rgba(255,255,255,0.18); color: #fff; border: none; border-radius: 6px; padding: 0.35rem 0.8rem; cursor: pointer; font-size: 0.85rem; }
  main { flex: 1; max-width: 820px; width: 100%; margin: 0 auto; padding: 1.5rem; }
  .msg { margin-bottom: 1rem; display: flex; }
  .msg .bubble { max-width: 85%; padding: 0.7rem 1rem; border-radius: 12px; white-space: pre-wrap; line-height: 1.45; font-size: 0.95rem; }
  .msg.user { justify-content: flex-end; }
  .msg.user .bubble { background: var(--accent); color: #fff; border-bottom-right-radius: 4px; }
  .msg.assistant .bubble { background: #fff; border: 1px solid var(--border); border-bottom-left-radius: 4px; }
  .msg.error .bubble { background: #fdeaea; border: 1px solid #e8b4b4; color: #8a2f2f; }
  .tools { margin-top: 0.5rem; display: flex; gap: 0.35rem; flex-wrap: wrap; }
  .tools .pill { background: var(--accent-light); color: var(--accent); border-radius: 999px; padding: 0.1rem 0.55rem; font-size: 0.72rem; font-weight: 600; }
  .empty { text-align: center; color: var(--muted); margin-top: 3rem; }
  .empty ul { list-style: none; padding: 0; }
  .empty li { background: #fff; border: 1px solid var(--border); border-radius: 8px; padding: 0.5rem 0.9rem; margin: 0.4rem auto; max-width: 460px; font-size: 0.9rem; }
  .composer { position: sticky; bottom: 0; background: #fafafa; padding: 1rem 1.5rem 1.5rem; border-top: 1px solid var(--border); }
  .composer form { max-width: 820px; margin: 0 auto; display: flex; gap: 0.6rem; }
  .composer input { flex: 1; padding: 0.7rem 1rem; border: 1px solid var(--border); border-radius: 10px; font-size: 0.95rem; }
  .composer input:focus { outline: 2px solid var(--accent); border-color: transparent; }
  .composer button { background: var(--accent); color: #fff; border: none; border-radius: 10px; padding: 0.7rem 1.4rem; font-size: 0.95rem; cursor: pointer; }
</style>
</head>
<body>
<header>
  <h1>Sassenberger Triathlon · Assistant</h1>
  <span>answers via MCP tools</span>
  <form action="/reset" method="post"><button type="submit">New conversation</button></form>
</header>
<main>
  <#if history?size == 0>
    <div class="empty">
      <p>Ask me anything about the Sassenberger Triathlon, for example:</p>
      <ul>
        <li>Which distances can I race in Sassenberg?</li>
        <li>Wie melde ich mich an — gibt es noch freie Plätze?</li>
        <li>Who won the Mitteldistanz in 2025?</li>
        <li>Wie oft ist "Müller" schon in Sassenberg gefinisht?</li>
      </ul>
    </div>
  </#if>
  <#list history as entry>
    <div class="msg ${entry.role()}">
      <div>
        <div class="bubble">${entry.text()}</div>
        <#if entry.toolsUsed()?size gt 0>
          <div class="tools">
            <#list entry.toolsUsed() as tool><span class="pill">${tool}</span></#list>
          </div>
        </#if>
      </div>
    </div>
  </#list>
</main>
<div class="composer">
  <form action="/chat" method="post">
    <input type="text" name="message" placeholder="Ask about the event, distances, registration or results…" autofocus autocomplete="off">
    <button type="submit">Send</button>
  </form>
</div>
</body>
</html>
