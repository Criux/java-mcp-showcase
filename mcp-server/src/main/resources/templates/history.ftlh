<#import "layout.ftlh" as layout>
<@layout.page title="History" active="history">
  <style>
    .exchange { background: #fff; border: 1px solid var(--border); border-radius: 10px; padding: 0.7rem 1rem; margin-bottom: 0.8rem; }
    .exchange .meta { display: flex; gap: 0.8rem; flex-wrap: wrap; align-items: baseline; font-size: 0.85rem; margin-bottom: 0.4rem; }
    .exchange .meta .summary { font-weight: 600; }
    .exchange .meta .muted { font-size: 0.8rem; }
    .status-ok { color: var(--accent); font-weight: 600; }
    .status-err { color: #a33; font-weight: 600; }
    details.payload { margin: 0.25rem 0; }
    details.payload summary { display: flex; gap: 0.5rem; align-items: baseline; cursor: pointer; list-style: none; }
    details.payload summary::before { content: "▸"; color: var(--muted); flex: 0 0 auto; }
    details.payload[open] summary::before { content: "▾"; }
    details.payload summary .dir { flex: 0 0 5.2rem; font-size: 0.78rem; font-weight: 600; color: var(--muted); text-transform: uppercase; }
    details.payload summary code.oneline { flex: 1 1 auto; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; background: var(--accent-light); padding: 0.15rem 0.5rem; border-radius: 4px; font-size: 0.8rem; }
    details.payload[open] summary code.oneline { display: none; }
    details.payload pre { background: #f4f7f6; border: 1px solid var(--border); border-radius: 6px; padding: 0.7rem 0.9rem; font-size: 0.78rem; overflow-x: auto; margin: 0.3rem 0 0.3rem 1.1rem; }
    .toolbar { display: flex; align-items: baseline; gap: 1rem; }
    .toolbar form { margin-left: auto; }
    .toolbar button { background: var(--accent); color: #fff; border: none; border-radius: 6px; padding: 0.4rem 0.9rem; cursor: pointer; font-size: 0.85rem; }
  </style>

  <div class="toolbar">
    <h2>MCP traffic history (${exchanges?size})</h2>
    <form action="/history/clear" method="post"><button type="submit">Clear history</button></form>
  </div>
  <p class="muted">Every message exchanged between MCP clients and this server over
    <code>${mcpEndpoint}</code>, newest first (last 500). Click a payload to expand it.</p>

  <#if exchanges?size == 0>
    <p class="muted">No MCP traffic recorded yet — connect a client or run an MCP handshake
       against <code>${mcpEndpoint}</code>.</p>
  </#if>

  <#list exchanges as e>
    <div class="exchange">
      <div class="meta">
        <span class="muted">#${e.id()}</span>
        <span>${e.timestamp().toLocalTime()}</span>
        <span>${e.httpMethod()} ${e.path()}</span>
        <span class="<#if e.status() < 400>status-ok<#else>status-err</#if>">${e.status()}</span>
        <span class="muted">${e.durationMs()} ms</span>
        <#if e.shortSession() != ''><span class="muted">session ${e.shortSession()}</span></#if>
        <span class="summary">${e.summary()}</span>
      </div>
      <#if e.requestCompact() != ''>
        <details class="payload">
          <summary><span class="dir">Request</span><code class="oneline">${e.requestCompact()}</code></summary>
          <pre>${e.requestPretty()}</pre>
        </details>
      <#else>
        <div class="muted" style="font-size:0.8rem; margin-left:1.1rem">Request: (empty body)</div>
      </#if>
      <#if e.responseCompact() != ''>
        <details class="payload">
          <summary><span class="dir">Response</span><code class="oneline">${e.responseCompact()}</code></summary>
          <pre>${e.responsePretty()}</pre>
        </details>
      <#else>
        <div class="muted" style="font-size:0.8rem; margin-left:1.1rem">Response: (empty body)</div>
      </#if>
    </div>
  </#list>
</@layout.page>
