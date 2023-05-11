<cfoutput> 
<cfsavecontent variable="xml"><?xml version="1.0" encoding="ISO-8859-1"?>
	<!DOCTYPE foo [
	<!ELEMENT foo ANY >
	<!ENTITY xxe SYSTEM "http://update.lucee.org/rest/update/provider/echoGet/cgi" >]>
	<foo>&xxe;</foo>
</cfsavecontent> 
	<cftry> 
		<cfset results = xmlSearch(xml, "/foo")>
		<cfcatch> 
			#cfcatch.message#
		</cfcatch> 
 	</cftry> 
</cfoutput> 