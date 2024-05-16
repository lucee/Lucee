<cfparam name="form.scene" default="">

<cfif form.scene eq 1>
	<cftry>
		
		<cfinvoke
		component="test"
		method="getRole"
		returnVariable="output">
		<cfoutput>#output#</cfoutput>
		<cfcatch type="any" name="e">
			<cfoutput>The current user is not authorized to invoke this method.</cfoutput>
	 	</cfcatch>
	</cftry>
<cfelseif form.scene eq 2>
	<cfloginuser name="Test" password="dummy" roles="test">
	<cfinvoke
	component="test"
	method="getRole"
	returnVariable="output">
	<cfoutput>#output#</cfoutput>
</cfif>