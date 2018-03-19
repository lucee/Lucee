<cfset name = "test">
<cfparam name="form.scene" default="1">
<cfif form.scene EQ 1>
	<cfscript>
		result = QueryExecute("select *
			from (select *
			from
			LDEV1740
			-- can't use [OUTPUT Inserted.Email] because of trigger
			where name=:newName) test", {newName = name});
	</cfscript>
<cfelse>
	<cfquery name="result">
		select *
		from (select *
		 from
		 LDEV1740
		 -- can't use [OUTPUT Inserted.Email] because of trigger
		 where name= <cfqueryparam cfsqltype="cf_sql_varchar" value="#name#" /> ) test
	</cfquery>
</cfif>
<cfoutput>#isQuery(result)#</cfoutput>
