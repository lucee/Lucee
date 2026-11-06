<cfscript>
	def = { "component": "engines.Engine", "custom": { "session": "EchoSession", "label": "ldev6131-tag-" & createUUID(), "model": "echo" } };
</cfscript>
<cfLuceeAI name="#def#" message="be terse" meta="md"><cfLuceeAIInquiry question="hello" answer="ans"></cfLuceeAI>
<cfoutput>#serializeJSON( { "answer": ans, "label": md.label } )#</cfoutput>
