<cffunction name="Case9">
	<cfquery name="local.qData" result="result" dbtype="query">
		select * from allCountries
	</cfquery>

	<cfset expect(isNull(variables.result)).toBeFalse()>
	<cfset expect(isNull(local.result)).toBeTrue()>
	<cfset expect(NOT isNull(variables.result) && structKeyExists(variables.result, "RecordCount") && variables.result.RecordCount EQ 2).toBeTrue()>
</cffunction>

<cffunction name="Case10">
	<cfquery name="local.qData" result="local.result" dbtype="query">
		select * from allCountries
	</cfquery>

	<cfset expect(isNull(variables.result)).toBeTrue()>
	<cfset expect(isNull(local.result)).toBeFalse()>
	<cfset expect(NOT isNull(local.result) && structKeyExists(local.result, "RecordCount") && local.result.RecordCount EQ 2).toBeTrue()>
</cffunction>

<cffunction name="Case11">
	<cfset var result = {}>
	<cfquery name="local.qData" result="result" dbtype="query">
		select * from allCountries
	</cfquery>

	<cfset expect(isNull(variables.result)).toBeTrue()>
	<cfset expect(isNull(local.result)).toBeFalse()>
	<cfset expect(NOT isNull(local.result) && structKeyExists(local.result, "RecordCount") && local.result.RecordCount EQ 2).toBeTrue()>
</cffunction>

<cffunction name="Case12">
	<cfset var result = {}>
	<cfquery name="local.qData" result=local.result dbtype="query">
		select * from allCountries
	</cfquery>

	<cfset expect(isNull(variables.result)).toBeTrue()>
	<cfset expect(isNull(local.result)).toBeFalse()>
	<cfset expect(NOT isNull(local.result) && structKeyExists(local.result, "RecordCount") && local.result.RecordCount EQ 2).toBeTrue()>
</cffunction>

<cffunction name="Case13">
	<cfset result = {}>
	<cfquery name="local.qData" result=result dbtype="query">
		select * from allCountries
	</cfquery>

	<cfset expect(isNull(variables.result)).toBeFalse()>
	<cfset expect(isNull(local.result)).toBeTrue()>
	<cfset expect(NOT isNull(variables.result) && structKeyExists(variables.result, "RecordCount") && variables.result.RecordCount EQ 2).toBeTrue()>
</cffunction>

<cffunction name="Case14">
	<cfset var isSuccess = true>
	<cftry>
		<cfquery name="local.qData" result=result dbtype="query">
			select * from allCountries
		</cfquery>
		<cfcatch type="any">
			<cfset isSuccess = false>
		</cfcatch>
	</cftry>
	<cfset expect(isSuccess).toBeTrue()>
</cffunction>

<cffunction name="Case15">
	<cfset var foo = {}>
	<cfquery name="local.qData" result="foo.result" dbtype="query">
		select * from allCountries
	</cfquery>

	<cfset expect(isNull(variables.foo.result)).toBeTrue()>
	<cfset expect(isNull(local.foo.result)).toBeFalse()>
	<cfset expect(NOT isNull(local.foo.result) && structKeyExists(local.foo.result, "RecordCount") && local.foo.result.RecordCount EQ 2).toBeTrue()>
</cffunction>

<cffunction name="Case16">
	<cfset foo = {}>
	<cfquery name="local.qData" result="foo.result" dbtype="query">
		select * from allCountries
	</cfquery>

	<cfset expect(isNull(variables.foo.result)).toBeFalse()>
	<cfset expect(isNull(local.foo.result)).toBeTrue()>
	<cfset expect(NOT isNull(variables.foo.result) && structKeyExists(variables.foo.result, "RecordCount") && variables.foo.result.RecordCount EQ 2).toBeTrue()>
</cffunction>