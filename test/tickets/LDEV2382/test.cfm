<cfparam name="form.scene" default="">

<cfset testquery = querynew( "id,alphabets","integer,varchar",[
		{"id":1,"alphabets":"A"},{"id":2,"alphabets":"C"},{"id":2,"alphabets":"B"},{"id":2,"alphabets":"A"},{"id":2,"alphabets":"C"}
		,{"id":2,"alphabets":"C"},{"id":2,"alphabets":"A"},{"id":2,"alphabets":"A"},{"id":2,"alphabets":"A"}
	])>
<cfset testType = #form.type#>
<cfif form.scene eq '1'>
	<cfquery name="getData" dbtype="query">
		SELECT DISTINCT #testType#(ALPHABETS)as ALPHABETS FROM TESTQUERY
	</cfquery>
	<cfoutput query="getData">#getData.alphabets#</cfoutput>
</cfif>
<cfif form.scene eq '2'>
	<cfquery name="getDatarc" dbtype="query">
		SELECT DISTINCT #testType#(ALPHABETS)as ALPHABETS FROM TESTQUERY
	</cfquery>
	<cfoutput>#getDatarc.recordcount#</cfoutput>
</cfif>