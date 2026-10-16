<cfparam name="FORM.Scene" default="1">
<cffunction name="isDataAvail" access="public" returntype="boolean">
	<cfquery name="tmpQry">
			SELECT CAST(OBJECT_NAME AS varchar2(50)) || '|' || CAST(OBJECT_TYPE AS varchar2(50)) || '|' || CAST( OWNER AS varchar2(30) ) AS Name FROM ALL_OBJECTS WHERE OBJECT_NAME = 'LDEV1147_PKG' AND STATUS = 'VALID'
	</cfquery>
	<cfset tmpList = valueList(tmpQry.Name)>
	<cfset variables.Owner = listLast(tmpList, "|")>
	<cfreturn !(tmpQry.RecordCount == 2 && findNoCase("LDEV1147_PKG|PACKAGE", tmpList) && findNoCase("LDEV1147_PKG|PACKAGE BODY", tmpList))>
</cffunction>

<cffunction name="PackageWithoutParameter">
	<cfif isDataAvail()>
		<cfabort />
	</cfif>
	<cfset hasError = "False">
	<cftry>
		<cfstoredproc procedure="#variables.Owner#.ldev1147_pkg.testproc">
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
		<cfstoredproc procedure="#variables.Owner#.ldev1147_pkg.testproc2" >
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
		<cfstoredproc procedure="#variables.Owner#.LDEV1147_SYN##.testproc" >
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
		<cfstoredproc procedure="#variables.Owner#.LDEV1147_SYN##.testproc2" >
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