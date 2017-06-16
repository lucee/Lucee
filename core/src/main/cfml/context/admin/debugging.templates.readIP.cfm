<cfparam name="ipEdit[1].XmlAttributes.id" default="">
<cfparam name="ipEdit[1].XmlText" default="">
<cfif request.adminType EQ "web">
	<cfset filePath = getPageContext().getConfig().getConfigDir().getRealResource("lucee-web.xml.cfm")>
<cfelse>
	<cfset filePath = "#replaceNocase(expandPath("/lucee-server"), "\context\context", "\context")#\lucee-server.xml" />
</cfif>
<cfset xmlObj = xmlParse(fileRead(filePath))>
<cfset xmlElem = XmlSearch(xmlObj, '//*[ local-name()=''ipresrtriction'' ]')>