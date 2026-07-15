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
  .msg .bubble { max-width: 85%; padding: 0.7rem 1rem; border-radius: 12px; line-height: 1.45; font-size: 0.95rem; overflow-wrap: anywhere; }
  .msg.user { justify-content: flex-end; }
  .msg.user .bubble { background: var(--accent); color: #fff; border-bottom-right-radius: 4px; white-space: pre-wrap; }
  /* markdown content inside assistant bubbles */
  .bubble > *:first-child { margin-top: 0; }
  .bubble > *:last-child { margin-bottom: 0; }
  .bubble p, .bubble ul, .bubble ol, .bubble pre, .bubble table, .bubble blockquote { margin: 0.5rem 0; }
  .bubble ul, .bubble ol { padding-left: 1.3rem; }
  .bubble li { margin: 0.15rem 0; }
  .bubble h1, .bubble h2, .bubble h3, .bubble h4 { margin: 0.7rem 0 0.35rem; font-size: 1.02em; }
  .bubble code { background: var(--accent-light); padding: 0.1rem 0.35rem; border-radius: 4px; font-size: 0.85em; }
  .bubble pre { background: #f4f7f6; border: 1px solid var(--border); border-radius: 6px; padding: 0.6rem 0.8rem; overflow-x: auto; }
  .bubble pre code { background: none; padding: 0; }
  .bubble a { color: var(--accent); }
  .bubble table { border-collapse: collapse; }
  .bubble th, .bubble td { border: 1px solid var(--border); padding: 0.25rem 0.55rem; font-size: 0.88em; }
  .bubble blockquote { border-left: 3px solid var(--border); padding-left: 0.7rem; color: var(--muted); }
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
  .composer button:disabled { opacity: 0.5; cursor: default; }
  .typing-dots { display: inline-flex; gap: 5px; align-items: center; padding: 0.3rem 0; }
  .typing-dots span { width: 7px; height: 7px; border-radius: 50%; background: var(--muted); animation: dot-blink 1s infinite; }
  .typing-dots span:nth-child(2) { animation-delay: 0.2s; }
  .typing-dots span:nth-child(3) { animation-delay: 0.4s; }
  @keyframes dot-blink { 0%, 80%, 100% { opacity: 0.25; transform: translateY(0); } 40% { opacity: 1; transform: translateY(-2px); } }
</style>
</head>
<body>
<header>
  <h1>Sassenberger Triathlon · Assistant</h1>
  <span>answers via MCP tools</span>
  <form action="/reset" method="post"><button type="submit">New conversation</button></form>
</header>
<main id="messages">
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
  <form id="chat-form" action="/chat" method="post">
    <input id="chat-input" type="text" name="message" placeholder="Ask about the event, distances, registration or results…" autofocus autocomplete="off">
    <button id="chat-send" type="submit">Send</button>
  </form>
</div>
<script src="/js/marked.min.js"></script>
<script>
<#noparse>
(function () {
  const form = document.getElementById('chat-form');
  const input = document.getElementById('chat-input');
  const send = document.getElementById('chat-send');
  const messages = document.getElementById('messages');
  const TYPE_INTERVAL_MS = 35; // per word

  function scrollDown() {
    window.scrollTo(0, document.body.scrollHeight);
  }

  // Renders assistant markdown to HTML. Raw HTML in the source is escaped
  // first, so only markdown syntax can produce elements.
  function renderMarkdown(text) {
    const escaped = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return marked.parse(escaped, { gfm: true, breaks: true });
  }

  function addMessage(role) {
    const msg = document.createElement('div');
    msg.className = 'msg ' + role;
    const inner = document.createElement('div');
    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    inner.appendChild(bubble);
    msg.appendChild(inner);
    messages.appendChild(msg);
    return { msg, inner, bubble };
  }

  function addTools(inner, tools) {
    if (!tools || tools.length === 0) return;
    const box = document.createElement('div');
    box.className = 'tools';
    for (const tool of tools) {
      const pill = document.createElement('span');
      pill.className = 'pill';
      pill.textContent = tool;
      box.appendChild(pill);
    }
    inner.appendChild(box);
  }

  function addLoading() {
    const m = addMessage('assistant');
    m.bubble.innerHTML = '<span class="typing-dots"><span></span><span></span><span></span></span>';
    return m;
  }

  // Types the text into the bubble word by word, re-rendering the
  // accumulated markdown on every tick so formatting appears live.
  function typeText(m, role, text, tools) {
    m.msg.className = 'msg ' + role;
    m.bubble.textContent = '';
    const words = text.match(/\S+\s*/g) || [text];
    let accumulated = '';
    let i = 0;
    const timer = setInterval(() => {
      if (i >= words.length) {
        clearInterval(timer);
        m.bubble.innerHTML = renderMarkdown(text);
        addTools(m.inner, tools);
        scrollDown();
        return;
      }
      accumulated += words[i++];
      m.bubble.innerHTML = renderMarkdown(accumulated);
      scrollDown();
    }, TYPE_INTERVAL_MS);
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const message = input.value.trim();
    if (!message) return;

    document.querySelector('.empty')?.remove();
    addMessage('user').bubble.textContent = message;
    input.value = '';
    input.focus();
    send.disabled = true;
    const loading = addLoading();
    scrollDown();

    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ message })
      });
      if (!response.ok) throw new Error('HTTP ' + response.status);
      const data = await response.json();
      typeText(loading, data.role, data.text, data.toolsUsed);
    } catch (error) {
      typeText(loading, 'error', 'The request failed: ' + error.message, []);
    } finally {
      send.disabled = false;
      input.focus();
    }
  });

  // Server-rendered history arrives as escaped plain text — convert the
  // assistant/error bubbles to rendered markdown on load.
  document.querySelectorAll('.msg.assistant .bubble, .msg.error .bubble').forEach((bubble) => {
    bubble.innerHTML = renderMarkdown(bubble.textContent);
  });

  scrollDown();
})();
</#noparse>
</script>
</body>
</html>
