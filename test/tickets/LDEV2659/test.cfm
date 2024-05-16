<cfoutput>
	<cfparam name="form.scene" default="">
	<cfif form.scene eq 1>
		<cftry>
			#test#
			<cfcatch type="any" name="variables.error">
				<cfset test = variables.error>
				#test.message#
			</cfcatch>
		</cftry>
	</cfif>
</cfoutput>

<cfif form.scene eq 2>
	<cfscript>
		try{
			writeOutput("#test#");
		}
		catch(any variables.thiserror){
			getError = variables.thiserror;
			writeOutput("#getError.message#");
		}
	</cfscript>
</cfif>

