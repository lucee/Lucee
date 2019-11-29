<cfparam name="form.scene" default="">
<cfif form.scene eq 1>
	<cfquery name="test1" datasource="LDEV2549_DSN">
		select * from mytest
	</cfquery>

	<cfloop query="#test1#">
		<cfquery name="test2" datasource="LDEV2549_DSN">
			SELECT COUNT(DISTINCT id) AS deviceCount, MAX(id) AS maxDeviceID FROM LDEV2549 WHERE day = <cfqueryparam value="#test1.day#">
		</cfquery>
		<cfoutput>#test2.maxDeviceID#</cfoutput>
	</cfloop>
</cfif>

<cfif form.scene eq 2>
	<cfquery name="test1" datasource="LDEV2549_DSN">
		select * from mytest
	</cfquery>
	<cfloop query="#test1#" group="day">
		<cfquery name="test2" datasource="LDEV2549_DSN">
			SELECT COUNT(DISTINCT id) AS deviceCount, MAX(id) AS maxDeviceID FROM LDEV2549 WHERE day = <cfqueryparam cfsqltype="cf_sql_date" value="#test1.day#">
		</cfquery>
		<cfoutput>#test2.maxDeviceID#</cfoutput>
	</cfloop>
</cfif>
