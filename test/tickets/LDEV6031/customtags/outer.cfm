<!--- Outer wrapper tag --->
<cfif thisTag.executionMode eq "start">
	<cfoutput><div class="outer"></cfoutput>
<cfelse>
	<cfoutput></div></cfoutput>
</cfif>
