<cfsetting showdebugoutput="no">
<cfparam name="FORM.Scene" default="1">
<cfif FORM.Scene EQ 1>
	<cfscript>
		pm = entityNew("PageMapping");
		ormreload();
		result = OrmExecuteQuery( "FROM PageMapping" );
	</cfscript>
<cfelse>
	<!--- HQL using query tag --->
	<cfquery name="result" dbtype="hql">
		FROM PageMapping
	</cfquery>
</cfif>
