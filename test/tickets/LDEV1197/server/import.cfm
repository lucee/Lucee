<cftry>
	<cfimport taglib="/s1197/lib" prefix="t">
	<t:redden message="Mapping in server work fine on cfimport">
	<cfcatch type="any" >
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
</cftry>
