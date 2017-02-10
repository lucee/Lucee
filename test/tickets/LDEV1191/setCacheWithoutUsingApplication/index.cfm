<cffunction name="testFunct" access="public" cachedwithin="#createTimeSpan(0,0,1,0)#">
	<cfset a = 10>
	<cfreturn a>
</cffunction>

<cfquery name="queryData" cachedwithin="#createTimeSpan(0,0,1,0)#">
	Select * from usersDetails
</cfquery>

<cfinclude template="includeFile.cfm" cachedwithin="#createTimeSpan(0,0,1,0)#">

<cfset testFunct() />

<cfobjectcache action="size" type="query" result="queryCache"/>
<cfobjectcache action="size" type="include" result="includeCache"/>
<cfobjectcache action="size" type="function" result="functionCache"/>

<cfobjectcache action="clear" type="query"/>
<cfobjectcache action="clear" type="include"/>
<cfobjectcache action="clear" type="function"/>

<cfoutput>#queryCache#|#includeCache#|#functionCache#</cfoutput>
