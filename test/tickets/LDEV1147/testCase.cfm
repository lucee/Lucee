<cfparam name="FORM.Scene" default="1">
<cffunction name="isDataAvail" access="public" returntype="boolean">
	<cfquery name="tmpQry">
			SELECT CAST(OBJECT_NAME AS varchar2(50)) || '|' || CAST(OBJECT_TYPE AS varchar2(50)) || '|' || CAST( OWNER AS varchar2(30) ) AS Name FROM ALL_OBJECTS WHERE OBJECT_NAME = 'LUCEE_BUG_TEST' AND STATUS = 'VALID'
	</cfquery>
	<cfset tmpList = valueList(tmpQry.Name)>
	<cfset variables.Owner = listLast(tmpList, "|")>
	<cfreturn !(tmpQry.RecordCount == 2 && findNoCase("LUCEE_BUG_TEST|PACKAGE", tmpList) && findNoCase("LUCEE_BUG_TEST|PACKAGE BODY", tmpList))>
</cffunction>

<cffunction name="PackageWithoutParameter">
	<cfif isDataAvail()>
		<cfabort />
	</cfif>
	<cfset hasError = "False">
	<cftry>
		<cfstoredproc procedure="#variables.Owner#.lucee_bug_test.testproc">
		<cfcatch>
			<cfset hasError = cfcatch.detail>
		</cfcatch>
	</cftry>
	<cfreturn hasError>
</cffunction>

<cffunction name="PackageWithParameter" access="public">
	<cfif isDataAvail()>
		<cfabort />
	</cfif>
	<cfset hasError = "False">
	<cftry>
		<cfstoredproc procedure="#variables.Owner#.lucee_bug_test.testproc2" >
			<cfprocparam cfsqltype="cf_sql_varchar" value="foo">
		</cfstoredproc>
		<cfcatch>
			<cfset hasError = cfcatch.detail>
		</cfcatch>
	</cftry>
	<cfreturn hasError>
</cffunction>

<cffunction name="synonymWithoutParameter" access="public">
	<cfif isDataAvail()>
		<cfabort />
	</cfif>
	<cfset hasError = "False">
	<cftry>
		<cfstoredproc procedure="#variables.Owner#.BU##.testproc" >
		<cfcatch>
			<cfset hasError = cfcatch.detail>
		</cfcatch>
	</cftry>
	<cfreturn hasError>
</cffunction>

<cffunction name="synonymWithParameter" access="public">
	<cfif isDataAvail()>
		<cfabort />
	</cfif>
	<cfset hasError = "False">
	<cftry>
		<cfstoredproc procedure="#variables.Owner#.BU##.testproc2" >
			<cfprocparam cfsqltype="cf_sql_varchar" value="foo">
		</cfstoredproc>
		<cfcatch>
			<cfset hasError = cfcatch.detail>
		</cfcatch>
	</cftry>
	<cfreturn hasError>
</cffunction>

<cfif FORM.Scene EQ 1>
	<cfset result = PackageWithoutParameter()>
<cfelseif FORM.Scene EQ 2>
	<cfset result = PackageWithParameter()>
<cfelseif FORM.Scene EQ 3>
	<cfset result = synonymWithoutParameter()>
<cfelseif FORM.Scene EQ 4>
	<cfset result = synonymWithParameter()>
</cfif>
<cfoutput>#result#</cfoutput>