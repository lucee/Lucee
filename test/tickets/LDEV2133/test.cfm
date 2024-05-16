<cfparam name="form.scene" default="">
<cfif form.scene EQ 1>
<cfscript>
	abort type="page";
</cfscript>
<cfelseif form.scene EQ 2>
<cfscript>
	abort type="request";
</cfscript>
<cfelseif form.scene EQ 3>
	<cfabort type="page"/>
<cfelseif form.scene EQ 4>
	<cfabort type="request"/>
</cfif>