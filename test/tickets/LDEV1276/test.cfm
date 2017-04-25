<cfparam name="form.scene" default="1">
<cfif form.scene EQ 1>
	<cf_redden message="custom tag used in tag">
<cfelseif form.scene EQ 2>
	<cfscript>
		cf_redden (message="custom tag used inside cfscript");
	</cfscript>
</cfif>