<#import "layout.ftlh" as layout>
<@layout.page title="Dashboard" active="dashboard">
  <h2>Overview</h2>
  <div class="cards">
    <div class="card"><div class="num">${tools?size}</div><div class="label">MCP tools</div></div>
    <div class="card"><div class="num">${raceCount}</div><div class="label">Races / distances</div></div>
    <div class="card"><div class="num">${yearCount}</div><div class="label">Result editions (years)</div></div>
    <div class="card"><div class="num">${totalResults}</div><div class="label">Result rows bundled</div></div>
  </div>

  <h2>Event</h2>
  <table>
    <tr><th>Event</th><td>${event.name()} (${event.edition()}. Auflage)</td></tr>
    <tr><th>Date</th><td>${event.date()}</td></tr>
    <tr><th>Venue</th><td>${event.venue()}</td></tr>
    <tr><th>Location</th><td>${event.location()}</td></tr>
    <tr><th>Organizer</th><td>${event.organizer().name()}, ${event.organizer().address()}</td></tr>
    <tr><th>Website</th><td><a href="${event.websiteUrl()}">${event.websiteUrl()}</a></td></tr>
    <tr><th>Registration</th><td>${registration.status()}</td></tr>
  </table>

  <h2>MCP tools</h2>
  <div class="tablewrap">
  <table>
    <tr><th>Tool</th><th>Description</th></tr>
    <#list tools as tool>
      <tr><td><code>${tool.name()}</code></td><td>${tool.description()}</td></tr>
    </#list>
  </table>
  </div>
</@layout.page>
