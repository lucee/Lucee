<cfcomponent output="false">

	
	<!---get--->
	<cffunction name="get" access="remote" output="false" returntype="string" returnformat="plain">
		<cfargument name="lib" type="string">
		
		<!--- restrict to files from JS directory !--->
		<cfif arguments.lib CT "..">
			<cfheader statuscode="400">
			<cfreturn "// 400 - Bad Request">
		</cfif>
		
		<cfset var relPath = "js/#arguments.lib#.js">

		<cfif fileExists( expandPath( relPath ) )>

			<cfcontent type="text/javascript">
			<cfsavecontent variable="local.result"><cfinclude template="#relPath#"></cfsavecontent>
			<cfreturn result>
		<cfelse>
			
			<cfheader statuscode="404">
			<cfreturn "// 404 - Not Found">
		</cfif>
	</cffunction>

	
</cfcomponent>