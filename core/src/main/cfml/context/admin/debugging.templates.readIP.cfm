<cfparam name="ipEdit[1].XmlAttributes.id" default="">
<cfparam name="ipEdit[1].XmlText" default="">
<cfif request.adminType EQ "web">
	<cfset filePath = "#expandpath("{lucee-config}\lucee-web.xml.cfm")#">
<cfelse>
	<cfset filePath = 	"#expandpath("{lucee-server}\lucee-server.xml")#">
</cfif>
<cfset xmlObj = xmlParse(fileRead(filePath))>
<cfset xmlElem = XmlSearch(xmlObj, '//*[ local-name()=''ipresrtriction'' ]')>

