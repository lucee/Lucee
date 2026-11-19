<cfif thisTag.executionMode eq "start">
	<cfif attributes.mode eq "set">
		<cfset caller[ attributes.target ] = attributes.value>
	<cfelseif attributes.mode eq "param">
		<cfparam name="caller.#attributes.target#" default="#attributes.value#">
	<cfelse>
		<cfset setVariable( "caller.#attributes.target#", attributes.value )>
	</cfif>
</cfif>
