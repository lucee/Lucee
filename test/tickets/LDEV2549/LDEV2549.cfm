<cfparam name="form.scene">
<cfquery name="test1" datasource="LDEV2549_DSN">
	select * from LDEV2549_2
</cfquery>

<cfloop query="#test1#">
	<cfscript>
		systemOutput("", true);
		systemOutput("#db#, #scene# [#test1.day#]", true);
		param = {
			value: test1.day
		};
		if (form.scene eq "date"){
			param.cfsqltype = "cf_sql_date";
		}
	</cfscript>
	<cftry>
		<cfquery name="test2" datasource="LDEV2549_DSN">
			SELECT	COUNT(DISTINCT id) AS deviceCount, MAX(id) AS maxDeviceID 
			FROM	LDEV2549
			WHERE	day = <cfqueryparam attributeCollection=#param#>
		</cfquery>
		<cfoutput>#test2.maxDeviceID#</cfoutput>
		<cfcatch>
			<cfscript>
				systemOutput(cfcatch, true);
			</cfscript>
			<cfrethrow>
		</cfcatch>
	</cftry>
	
</cfloop>