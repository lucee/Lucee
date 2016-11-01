<cfsetting showdebugoutput="no">
<cfparam name="URL.Scene" default="1">
<cfif URL.Scene EQ 1>
	<cfscript>
		pm = entityNew("PageMapping");
		ormreload();
		result = OrmExecuteQuery( "FROM PageMapping" );
	</cfscript>
<cfelse>
	<!--- HQL using query tag --->
	<cfquery name="result2" dbtype="hql">
		FROM PageMapping
	</cfquery>
</cfif>
