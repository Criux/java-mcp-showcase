<#macro page title active>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${title} · ${serverName}</title>
<style>
  :root { --accent: #0b6e4f; --accent-light: #e6f2ee; --border: #e2e2e2; --text: #222; --muted: #777; }
  * { box-sizing: border-box; }
  body { margin: 0; font-family: system-ui, -apple-system, "Segoe UI", sans-serif; color: var(--text); background: #fafafa; }
  header { background: var(--accent); color: #fff; padding: 0.9rem 1.5rem; display: flex; align-items: baseline; gap: 1.5rem; flex-wrap: wrap; }
  header h1 { font-size: 1.1rem; margin: 0; font-weight: 600; }
  header .version { opacity: 0.75; font-size: 0.85rem; }
  nav { display: flex; gap: 0.25rem; margin-left: auto; }
  nav a { color: #fff; text-decoration: none; padding: 0.35rem 0.8rem; border-radius: 6px; font-size: 0.9rem; }
  nav a.active, nav a:hover { background: rgba(255,255,255,0.18); }
  main { max-width: 1000px; margin: 1.5rem auto; padding: 0 1.5rem; }
  h2 { font-size: 1.25rem; margin: 1.6rem 0 0.7rem; }
  .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; }
  .card { background: #fff; border: 1px solid var(--border); border-radius: 10px; padding: 1rem 1.2rem; }
  .card .num { font-size: 1.7rem; font-weight: 700; color: var(--accent); }
  .card .label { color: var(--muted); font-size: 0.85rem; margin-top: 0.2rem; }
  table { width: 100%; border-collapse: collapse; background: #fff; border: 1px solid var(--border); border-radius: 10px; overflow: hidden; font-size: 0.9rem; }
  th, td { text-align: left; padding: 0.55rem 0.8rem; border-bottom: 1px solid var(--border); vertical-align: top; }
  th { background: var(--accent-light); font-weight: 600; }
  tr:last-child td { border-bottom: none; }
  code { background: var(--accent-light); padding: 0.1rem 0.4rem; border-radius: 4px; font-size: 0.85em; }
  .muted { color: var(--muted); }
  .pill { display: inline-block; background: var(--accent-light); color: var(--accent); border-radius: 999px; padding: 0.1rem 0.6rem; font-size: 0.78rem; font-weight: 600; }
  .tool { background: #fff; border: 1px solid var(--border); border-radius: 10px; padding: 1rem 1.2rem; margin-bottom: 1rem; }
  .tool h3 { margin: 0 0 0.4rem; font-size: 1rem; }
  .tool p { margin: 0.3rem 0 0.6rem; font-size: 0.9rem; }
  .tablewrap { overflow-x: auto; }
  footer { max-width: 1000px; margin: 2rem auto; padding: 0 1.5rem 2rem; color: var(--muted); font-size: 0.8rem; }
</style>
</head>
<body>
<header>
  <h1>${serverName}</h1>
  <span class="version">v${serverVersion} · ${protocol} · MCP endpoint <code style="background:rgba(255,255,255,0.18);color:#fff">${mcpEndpoint}</code></span>
  <nav>
    <a href="/" class="<#if active == 'dashboard'>active</#if>">Dashboard</a>
    <a href="/tools" class="<#if active == 'tools'>active</#if>">Tools</a>
    <a href="/data" class="<#if active == 'data'>active</#if>">Data</a>
    <a href="/history" class="<#if active == 'history'>active</#if>">History</a>
  </nav>
</header>
<main>
  <#nested>
</main>
<footer>
  MCP showcase · Sassenberger Triathlon · data transcribed from
  sassenbergertriathlon.de and mika:timing (read-only management interface)
</footer>
</body>
</html>
</#macro>
