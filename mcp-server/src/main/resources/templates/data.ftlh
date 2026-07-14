<#import "layout.ftlh" as layout>
<@layout.page title="Data" active="data">
  <h2>Races &amp; distances</h2>
  <div class="tablewrap">
  <table>
    <tr><th>Id</th><th>Race</th><th>Swim</th><th>Bike</th><th>Run</th><th>Fee</th><th>Eligibility</th><th>Time limit</th></tr>
    <#list races as race>
      <tr>
        <td><code>${race.id()}</code></td>
        <td>${race.name()}</td>
        <td>${race.swimKm()} km</td>
        <td>${race.bikeKm()} km</td>
        <td>${race.runKm()} km</td>
        <td><#if race.feeEur()??>${race.feeEur()} €<#else><span class="muted">–</span></#if></td>
        <td>${race.eligibility()}</td>
        <td><#if race.timeLimitMinutes()??>${race.timeLimitMinutes()} min<#else><span class="muted">–</span></#if></td>
      </tr>
    </#list>
  </table>
  </div>

  <h2>Registration</h2>
  <table>
    <tr><th>Platform</th><td>${registration.platform()}</td></tr>
    <tr><th>Online</th><td><a href="${registration.onlineUrl()}">${registration.onlineUrl()}</a></td></tr>
    <tr><th>Window</th><td>${registration.opens()} → ${registration.closes()}</td></tr>
    <tr><th>Status</th><td>${registration.status()}</td></tr>
  </table>

  <h2>Schedule</h2>
  <table>
    <#list schedule as day>
      <tr><th colspan="2">${day.day()} · ${day.date()}</th></tr>
      <#list day.items() as item>
        <tr><td style="white-space:nowrap">${item.time()}</td><td>${item.activity()}</td></tr>
      </#list>
    </#list>
  </table>

  <h2>Past results snapshots (${totalResults} rows)</h2>
  <p class="muted">${index.note()}</p>
  <#list index.years() as year>
    <h3 style="margin:1rem 0 0.4rem">${year.year()}
      <span class="muted" style="font-weight:normal;font-size:0.8rem">· ${year.sourceUrl()}</span></h3>
    <div class="tablewrap">
    <table>
      <tr><th>Code</th><th>Race</th><th>Results</th></tr>
      <#list year.events() as evt>
        <tr><td><code>${evt.code()}</code></td><td>${evt.name()}</td><td>${evt.resultCount()}</td></tr>
      </#list>
    </table>
    </div>
  </#list>
</@layout.page>
