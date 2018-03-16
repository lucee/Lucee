<cfset path = ListDeleteAt(getCurrenttemplatepath(), listLen(getCurrenttemplatepath(), "\"), "\") />
<cfparam name="FORM.Scene" default="1"> 
<cfset colName = "LDEV1745_0_0	">
<cfset criteria1 = '+"Test Words"' />
<cfset criteria2 = '+"Test Test Test"' />
<cfset criteria3 = '+"Test Test Test Test"' />
<cfset criteria4 = ' "Test Test Test Test"' />
<cfcollection action= "Create" collection="#colName#" path= "#path#" language="">
<cfindex collection="#colName#" action="refresh" key="#path#" type="path" urlpath="#path#" extensions=".cfm,.cfc,.pdf,.docx" >
<cftry>
	<cfif FORM.Scene EQ 1>
		<cfsearch name="srchValue" collection="#colName#" criteria= '#criteria1# '>
	<cfelseif FORM.Scene EQ 2>
		<cfsearch name="srchValue" collection="#colName#" criteria= '#criteria2# '>
	<cfelseif FORM.Scene EQ 3>
		<cfsearch name="srchValue" collection="#colName#" criteria= '#criteria3# '>
	<cfelseif FORM.Scene EQ 4>
		<cfsearch name="srchValue" collection="#colName#" criteria= '#criteria4# '>
	</cfif>
	<cfset result = srchValue.RecordCount> 
<cfcatch type="any">
	<cfset result ="#cfcatch.message#" />
</cfcatch>
</cftry>
<cfoutput>#result#</cfoutput>

<!--- <cfsearch name="result" collection="#colName#" critseria= '#criteria1# '> --->

<!--- <cfdump var="#result#" /> --->
<cfcollection action= "Delete" collection="#colName#">
