<cfparam name="url.mode" default="list">
<cfparam name="url.rating" default="low">
<cfif url.mode eq "list">
	<cfparam name="url.rating" set="low,medium, high">
<cfelseif url.mode eq "array">
	<cfparam name="url.rating" set="#['low','medium','high']#">
<cfelseif url.mode eq "default">
	<cfparam name="url.nothere" default="wrong" set="low,medium,high">
</cfif>
<cfoutput>#url.rating#</cfoutput>
