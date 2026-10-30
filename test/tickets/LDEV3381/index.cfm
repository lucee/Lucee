<cfquery name="q1">
	select cast(<cfqueryparam value="first" cfsqltype="cf_sql_varchar"> as varchar(20)) as ldev3381_marker
</cfquery>
<cfquery name="q2">
	select cast(<cfqueryparam value="second" cfsqltype="cf_sql_varchar"> as varchar(20)) as ldev3381_marker
</cfquery>
<cfquery name="q3">
	select 1 as ldev3381_noparam
</cfquery>
