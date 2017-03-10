<cfparam name="FORM.Scene" default="1">
<cfif FORM.Scene EQ 1>
	<cfscript>
		c = new content();
		q = queryNew( "cachable" );
		q.addRow();
		q.setCell( "cachable", 1 );
	</cfscript>
<cfelse>
	<cfset c = createObject("component", "content")>
	<cfset q = queryNew( "cachable" )>
	<cfset q.addRow()>
	<cfset q.setCell( "cachable", 1 )>
</cfif>
<cfloop query="q">
	<cftry>
		<cfset c.foo() />
		<cfcatch type="any">
			<cfoutput>#cfcatch.Message#</cfoutput>
		</cfcatch>
	</cftry>
</cfloop>