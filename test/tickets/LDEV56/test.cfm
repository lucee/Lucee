<cfparam name="form.scene" default="">
<cfif form.scene eq 1>
	<cfscript>
	try{
		throw(type="TestException", message="Testcase Passes");
	}catch(any e){
		writeOutput(e.message);
	}
	</cfscript>
</cfif>
<cfif form.scene eq 2>
	<cfoutput>
		<cftry>
			<cfthrow type="TestException" message="Testcase Passes">
			<cfcatch name="e">
				#e.message#
			</cfcatch>
		</cftry>
	</cfoutput>
</cfif>