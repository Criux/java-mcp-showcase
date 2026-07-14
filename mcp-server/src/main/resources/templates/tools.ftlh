<#import "layout.ftlh" as layout>
<@layout.page title="Tools" active="tools">
  <h2>Registered MCP tools (${tools?size})</h2>
  <p class="muted">Exposed over the MCP Streamable HTTP endpoint <code>${mcpEndpoint}</code>.
     All tools are read-only.</p>

  <#list tools as tool>
    <div class="tool">
      <h3><code>${tool.name()}</code>
        <#if tool.readOnly()><span class="pill">read-only</span></#if>
        <span class="muted" style="font-weight:normal;font-size:0.8rem">
          · ${tool.beanClass()} → ${tool.returnType()}</span>
      </h3>
      <p>${tool.description()}</p>
      <#if tool.params()?size gt 0>
        <div class="tablewrap">
        <table>
          <tr><th>Parameter</th><th>Type</th><th>Required</th><th>Description</th></tr>
          <#list tool.params() as param>
            <tr>
              <td><code>${param.name()}</code></td>
              <td>${param.type()}</td>
              <td><#if param.required()>yes<#else>no</#if></td>
              <td>${param.description()}</td>
            </tr>
          </#list>
        </table>
        </div>
      <#else>
        <p class="muted">No parameters.</p>
      </#if>
    </div>
  </#list>
</@layout.page>
