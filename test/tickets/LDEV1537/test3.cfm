<cftry>
	<cfset hasError = true>
    <cfmail from="aaa@bb.com" to="to:81@gmail.com" subject="sample" server="localhost">dummy email</cfmail>
	<cfcatch>
		<cfset hasError = false>
	</cfcatch>
</cftry>
<cfoutput>#hasError#</cfoutput>