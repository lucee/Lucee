<cfset res = {}>

<!--- explicit variables scope --->
<cftry>
	<cfset x = doesNotExist>
	<cfcatch type="any" name="Variables.ThisError">
		<cfset res.inVariables = isStruct( variables.ThisError ) && structKeyExists( variables.ThisError, "message" )>
		<cfset res.weirdKey = structKeyExists( variables, "Variables.ThisError" )>
	</cfcatch>
</cftry>

<!--- explicit request scope --->
<cftry>
	<cfset x = doesNotExist>
	<cfcatch type="any" name="request.ThisError">
		<cfset res.inRequest = isStruct( request.ThisError ) && structKeyExists( request.ThisError, "message" )>
	</cfcatch>
</cftry>

<!--- explicit local scope inside a function --->
<cffunction name="inFunction">
	<cfset var r = {}>
	<cftry>
		<cfset x = doesNotExist>
		<cfcatch type="any" name="local.ex">
			<cfset r.inLocal = isStruct( local.ex ) && structKeyExists( local.ex, "message" )>
			<cfset r.weirdKey = structKeyExists( local, "local.ex" )>
		</cfcatch>
	</cftry>
	<cfreturn r>
</cffunction>
<cfset res.fn = inFunction()>

<!--- plain name keeps working --->
<cftry>
	<cfset x = doesNotExist>
	<cfcatch type="any" name="plainError">
		<cfset res.plain = isStruct( plainError ) && structKeyExists( plainError, "message" )>
	</cfcatch>
</cftry>

<cfoutput>#serializeJSON( res )#</cfoutput>
