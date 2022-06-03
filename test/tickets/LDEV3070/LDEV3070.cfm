<cfparam name="form.scene" default="">
<cfparam name="form.returnType" default="query">

<cfif form.scene == "cfquery">
	<cfquery name="test" returnType="#form.returnType#">
		SELECT * FROM LDEV3070
	</cfquery>
	<cfoutput>#test.getDatasourceName()#</cfoutput>

<cfelseif form.scene == "queryExecute">
	
	<cfset res = queryExecute("SELECT * FROM LDEV3070", [], {returnType="#form.returnType#"})>
	<cfoutput>#res.getDatasourceName()#</cfoutput>

<cfelseif form.scene == "QoQ">
	<cfset qry = queryNew("test","varchar")>
	<cfquery name="result" returntype="query" dbtype="query">
		SELECT * FROM qry
	</cfquery>

	<cfoutput>#isNull(result.getDatasourceName())#</cfoutput>
</cfif>