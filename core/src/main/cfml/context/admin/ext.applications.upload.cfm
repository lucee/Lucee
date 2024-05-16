<!--- 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ---><!---
<cfoutput>#lCase(data.columnlist)#</cfoutput>
<cfdump var="#data#" />
<cfdump var="#extensions#" abort />
--->
<cfif not structKeyExists(form, "extfile") or form.extfile eq "">
	<cflocation url="#request.self#?action=#url.action#&noextfile=1" addtoken="no" />
</cfif>

<!--- try to upload (.zip and .re) --->
<cftry>
	<cffile action="upload" filefield="extfile" destination="#GetTempDirectory()#" nameconflict="forceunique" />
	<cfif cffile.serverfileext neq "lex">
		<cfthrow message="Only .lex is allowed as extension!" />
	</cfif>
	<cfcatch>
		<!--- try to delete the uploaded file, if any--->
		<cfif structKeyExists(variables, "cffile") and structKeyExists(variables.cffile, "serverfile")>
			<cftry>
				<cffile action="delete" file="#rereplace(cffile.serverdirectory, '[/\\]$', '')##server.separator.file##cffile.serverfile#" />
				<cfcatch><!--- too bad, but it'll just remain in the temp dir...---></cfcatch>
			</cftry>
		</cfif>
		<cfset printError(stText.ext.errorFileUpload & " #stText.ext.theerror#:<br /><em>#cfcatch.message# #cfcatch.detail#</em>") /><cfabort>
	</cfcatch>
</cftry>

<cfset zipfile = "#rereplace(cffile.serverdirectory, '[/\\]$', '')##server.separator.file##cffile.serverfile#" />
	
<!--- lex files --->
<cfif cffile.serverfileext eq "lex">
	<!--- move to deploy directory --->
	<cfadmin 
        action="updateRHExtension"
        type="#request.adminType#"
        password="#session["password"&request.adminType]#"
        
		source="#zipfile#">
	
	
	<!--- go back to overview --->
	<cflocation url="#request.self#?action=#url.action#&addedRe=true" addtoken="no" />
	

</cfif>




<!--- check the uploaded file: is it an extension? --->
<cfif not fileExists('zip://#zipfile#!config.xml')>
	<cfset fileDelete(zipfile) />
	<cfset printError(stText.ext.noConfigXMLInExtension) /><cfabort>
</cfif>

<!--- try to parse the config xml--->
<cftry>
	<cfset configXML = xmlParse('zip://#zipfile#!config.xml') />
	<cfcatch>
		<cfset fileDelete(zipfile) />
		<cfset printError(stText.ext.configNotParsed & ":<br /><em>#cfcatch.message# #cfcatch.detail#</em>") /><cfabort>
	</cfcatch>
</cftry>

<!--- xml tag available? --->
<cfif not structKeyExists(configXML, "config") or not structKeyExists(configxml.config, "info")>
	<cfset fileDelete(zipfile) />
	<cfset printError(stText.ext.configHasNoInfoTag & "<br />#stText.ext.reviewdocumentation#") /><cfabort>
</cfif>

<!--- test existence of necessary info data (should be done by xsl, but quicky quicky) --->
<cfloop list="id,name,label,author,version,created,type" index="key">
	<cfif not structKeyExists(configXml.config.info, key)>
		<cfset fileDelete(zipfile) />
		<cfset printError(replace(stText.ext.requiredKeyMissing, '$key$', key) & "<br />#stText.ext.reviewdocumentation#") /><cfabort>
	<cfelseif configXml.config.info[key].xmlText eq "">
		<cfset fileDelete(zipfile) />
		<cfset printError(replace(stText.ext.requiredKeyEmpty, '$key$', key) & "<br />#stText.ext.reviewdocumentation#") /><cfabort>
	</cfif>
</cfloop>

<!--- check for correct Type: web extensions should not be added to the server env. --->
<cfif not listFindNoCase("web,server,all", configXml.config.info.type.xmlText)>
	<cfset printError(stText.ext.typeFieldInvalid) />
	<cfset fileDelete(zipfile) /><cfabort>
</cfif>
<cfif configXml.config.info.type.xmlText neq "all" and configXml.config.info.type.xmlText neq request.adminType>
	<cfset fileDelete(zipfile) />
	<cfset printError(replaceList(stText.ext.adminNotEqualsInstallType, "$type$,$admintype$,$url.action$", "#uCase(configXml.config.info.type.xmlText)#,#uCase(request.adminType)#,#url.action#")) />
	<cfabort>
</cfif>

<!--- everything seems to be okay, so add it --->

<!--- create a UID which is used while installing --->

<!--- create an array which contains all the extension details --->
<!--- [query] is a query with one row, containing all extension info --->
<cfset qData = queryNew("type,id,name,label,description,version,category,image,download,paypal,author,codename,video,support,documentation,forum,mailinglist,network,created,provider,info,uid") />
<cfset queryAddRow(qData) />
<cfset session.uploadExtDetails = {
	  all: [qData]
	, data: qData
	, download: zipfile
} />
<cfloop collection="#configXml.config.info#" item="key">
	<cfif not structKeyExists(qData, key)>
		<cfcontinue />
	</cfif>
	<cfif isDefined("configXml.config.info[key].xmlText")>
		<cfset qData[key] = configXml.config.info[key].xmlText />
	<cfelse>
		<cfset qData[key] = "" />
	</cfif>
</cfloop>


<!--- set download URL for the extension --->
<cfset qData.download = zipfile />
<!--- DLM --->
<cfif qData.created eq "" or not isDate(qData.created)>
	<cfset qData.created = now() />
</cfif>
<!--- provider URL, + get/set info about the provider --->
<cfif not isValid('url', qData.provider) or not find('.cfc', qData.provider)>
	<cfset qData.provider = "manualupload" />
<cfelse>
	<cftry>
		<cfset datas=loadProvidersData(qData.provider)>
		<cfset data=datas[qData.provider]>
		<cfset qData.info = data.getInfo />
		<cfcatch></cfcatch>
	</cftry>
</cfif>

<cfif not isStruct(qData.info)>
	<!--- create 'manual' info --->
	<cfset qData.info = {
		  description: "Manual upload"
		, image: ""
		, lastmodified: now()
		, mode: "productive"
		, title: "Manual upload"
		, url: ""
	} />
</cfif>
<!--- set the UID --->
<cfset qData.uid = createId(qData.provider, qData.id) />

<!--- now redirect the user to the installation process --->
<cflocation url="#request.self#?action=#url.action#&action2=install1&uid=#qdata.uid#&uploadExt=1" addtoken="no" />
