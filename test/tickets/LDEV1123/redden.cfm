<!--- redden.cfm --->
<cftry >
	<cfimport taglib="lib" prefix="t">

	<t:redden message="Make this red">
	<cfcatch type="any">
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
</cftry>