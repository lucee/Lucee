<cfset caller["url.cfset"] = "url.cfset from custom tag">
<cfset param = "url.param">
<cfparam name="caller.#param#" DEFAULT = "url.param from custom tag">
<cfset setFunc = "url.setvariable">
<cfset setVariable("caller.#setFunc#","url.setVariable from custom tag")>
