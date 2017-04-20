<cftry>
	<cfimport taglib="/w1197/lib" prefix="t">
	<t:redden message="Mapping in web work fine on cfimport">
	<cfcatch type="any" >
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
</cftry>
