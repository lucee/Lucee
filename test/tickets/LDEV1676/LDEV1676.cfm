<cfoutput> 
<cfsavecontent variable="xml"><?xml version="1.0" encoding="ISO-8859-1"?>
	<!DOCTYPE foo [
	<!ELEMENT foo ANY >
	<!ENTITY xxe SYSTEM "https://update.lucee.org/rest/update/provider/getdate/5.3.9.2-SNAPSHOT" >]>
	<foo>&xxe;</foo>
</cfsavecontent> 
	<cftry> 
		<cfset results = xmlSearch(xml, "/foo")>
		<cfif serializejson(results) contains "August, 24 2021">
			<cfoutput>Vulnerable to XXE and accept the external entity: #results[1].XmlText#</cfoutput>
		</cfif>
		<cfcatch> 
			#cfcatch.message#
		</cfcatch> 
 	</cftry> 
</cfoutput> 