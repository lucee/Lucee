<cffunction name="testFunc" access="public">
	<cfset a = 10>
	<cfreturn a>
</cffunction>

<cfquery name="queryDatas">
	Select * from details where id=1
</cfquery>

<cfinclude template="includeFiles.cfm">

<cfset testFunc() />

<cfobjectcache action="size" type="query" result="queryAppCache"/>
<cfobjectcache action="size" type="include" result="includeAppCache"/>
<cfobjectcache action="size" type="function" result="functionAppCache"/>

<cfobjectcache action="clear" type="query"/>
<cfobjectcache action="clear" type="include"/>
<cfobjectcache action="clear" type="function"/>

<cfoutput>#queryAppCache#|#includeAppCache#|#functionAppCache#</cfoutput>