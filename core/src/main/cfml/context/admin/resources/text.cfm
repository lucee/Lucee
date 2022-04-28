<!--- TODO: cleanup! !--->
<!--- language files are deployed to {lucee-web}/context/admin/resources/language by ConfigWebFactory.java and are read from there !--->

<cfset sHelpURL = "https://www.lucee.org/help/stHelp.json">
<cfparam name="request.stLocalHelp" default="#structNew()#">
<cfparam name="request.stWebMediaHelp" default="#structNew()#">
<cfparam name="request.stWebHelp" default="#structNew()#">
<cfparam name="application.stText" default="#structNew()#">
<!---
<cfset structDelete(application, "stText")>
<cfset structDelete(application, "stWebHelp")>
--->

<cfif structKeyExists(form, "lang") ||  !structKeyExists(application, "languages") || !structKeyExists(application.stText, session.lucee_admin_lang) || structKeyExists(url, "reinit")>

	<cfinclude template="menu.cfm">
	<cfset langData  = getAvailableLanguages()>

	<cfset languages = {}>
	<cfloop collection="#langData#" item="value" index="key">
		
		<cfset languages[key] = value.name>
	</cfloop>
    <cfset application.languages = languages>

    <cfif !application.languages.keyExists( session.lucee_admin_lang )>
    	
    	<cfset systemOutput("Admin language file for [#session.lucee_admin_lang#] was not found, defaulting to English", true)>
    	<cfset session.lucee_admin_lang = "en">
    </cfif>

	<cfset application.stText.en = GetFromXMLNode( langData.en.xml.XMLRoot.XMLChildren )>

	<cfset StructDelete(application, "notTranslated")>
	
	<!--- now read the actual file when not english--->
    <cfif session.lucee_admin_lang != "en">

    	<cfset langXml = langData[ session.lucee_admin_lang ].xml>

        <cfset application.stText[ session.lucee_admin_lang ] = GetFromXMLNode( langXml.XMLRoot.XMLChildren, application.stText.en )>
	</cfif>

	<cfset stText = application.stText[ session.lucee_admin_lang ]>

    <cftry>
        <cfadmin 
        action="hasRemoteClientUsage"
        type="#request.adminType#"
        password="#session["password"&request.adminType]#"
        
        returnVariable="request.hasRemoteClientUsage">	
    	<cfcatch>
        	<cfset request.hasRemoteClientUsage=true>
        </cfcatch>
    </cftry>
    
    <cfset stText.menuStruct.web = createMenu(stText.menu,"web")>
	<cfset stText.menuStruct.server = createMenu(stText.menu, "server")>
    
<cfelse>
    <cfset languages=application.languages>
	<cfset stText = application.stText[session.lucee_admin_lang]>
</cfif>

<!--- TODO  what is thios good for? it does not work, URL does not exist
<cfif not structKeyExists(application, "stWebHelp") or structKeyExists(url, "reinit")>
	<cftry>
		<cfhttp url="#sHelpURL#" method="GET" timeout="1"></cfhttp>
		<cfset stHelp = deserializeJSON(cfhttp.filecontent)>
		<cfset application.stWebHelp = stHelp>
		<cfcatch>
			<cfset stHelp = {}>
		</cfcatch>
	</cftry>
<cfelse>
	<cfset stHelp = application.stWebHelp>
</cfif>	
<cfset request.stWebHelp = stHelp>
--->
<cfset request.stWebHelp = {}>


<!---
--->

<!--- 

You can use this code in order to write the structs into an XML file corresponding to the resources struct

<cfset stLang = {"de":"German","en":"English","nl":"Dutch"}>
<cfsavecontent variable="sXML"><cfoutput><?xml version="1.0" encoding="UTF-8"?>
<language key="#session.lucee_admin_lang#" label="#stLang[session.lucee_admin_lang]#">
#generateXML(stText)#</language></cfoutput></cfsavecontent>
<cffile action="WRITE" file="language/#session.lucee_admin_lang#.xml" output="#sXML#">
<cfabort>

<cffunction name="generateXML" returntype="string" output="no">
	<cfargument name="input" required="Yes">
	<cfparam name="request.level" default="0">
	<cfparam name="request.aPath" default="#arrayNew(1)#">
	<cfset request.level ++>
	<cfset var el = "">
	<cfset var sTab = Chr(9)>
	<cfset var sXML = "">
	<cfset var sCRLF = Chr(13) & Chr(10)>
	<cfif isSimpleValue(arguments.input)>
		<cfset sXML = sTab & "<data key=""" & buildKey() & """>" & XMLFormat(arguments.input) & "</data>" & sCRLF>
	<cfelseif isStruct(arguments.input)>
		<cfloop collection="#arguments.input#" item="el">
			<cfset request.aPath[request.level] = lCase(el)>
			<cfset sXML &= generateXML(arguments.input[el])>
		</cfloop>
	<cfelseif isArray(arguments.input)>
		<cfloop from="1" to="#arrayLen(arguments.input)#" index="el">
			<cfset request.aPath[request.level] = lCase(el)>
			<cfset sXML &= generateXML(arguments.input[el])>
		</cfloop>
	</cfif>
	<cfset request.level-- >
	<cfreturn sXML>
</cffunction>
--->

<cffunction name="GetFromXMLNode" returntype="any" output="No">
	<cfargument name="stXML" required="Yes">
	<cfargument name="base" required="no" default="#{}#" type="struct">
	
    <cfset var doCreate=false>
    <cfif not StructKeyExists(application,'notTranslated')>
    	<cfset application.notTranslated={}>
        <cfset var doCreate=true>
    </cfif>
	
	<cfset var el        = "">
	<cfset var stRet = arguments.base>
	<cfloop array="#arguments.stXML#" index="el">
		<cftry>
			<cfset variables.setStructElement(stRet, el.XMLAttributes.key, el.XMLText)>
            <cfif doCreate>
				<!--- <cfset application.notTranslated[el.XMLAttributes.key]=el.XMLText>--->
            <cfelse>
            	<cfset StructDelete(application.notTranslated,el.XMLAttributes.key,false)>
			</cfif>
			
			<cfcatch>
			</cfcatch>
		</cftry>
	</cfloop>
	<cfreturn stRet>
</cffunction>

<cffunction name="setHidden" output="No">
	<!--- hides several elements in the menu depending on the configuration --->
	<cfargument name="sMenu" required="Yes" type="string">
	<cfargument name="action" required="Yes" type="string">
	<cfargument name="hidden" required="Yes" type="boolean">
	<cfset var menu = "">
	<cfset var el = "">
	<cfloop array="#stText.MenuStruct[request.adminType]#" index="menu">
		<cfif menu.action eq arguments.sMenu>
			<cfloop array="#menu.children#" index="el">
				<cfif el.action eq arguments.action>
					<cfset el.hidden = arguments.hidden>
				</cfif>
			</cfloop>
		</cfif>
	</cfloop>
</cffunction>

<cffunction name="buildKey" returntype="string" output="No">
	<cfset var sRet = request.aPath[1]>
	<cfset var lst = "">
	<cfloop from="3" to="#request.level#" index="lst">
		<cfset sRet &= "." & request.aPath[lst - 1]>
	</cfloop>
	<cfreturn sRet>
</cffunction>

<cffunction name="setStructElement" output="no" returntype="struct">
	<cfargument name="st" required="Yes">
	<cfargument name="sKey" required="Yes">
	<cfargument name="value" required="Yes">
	<cfset var lst = "">
	<cfset var idx = "">
	<cfset var stTmp = {}>
	<cfset stTmp = arguments.st>
	<cfset idx = listGetAt(arguments.sKey, 1, ".")>
	<cfloop from="2" to="#ListLen(arguments.sKey, '.')#" index="lst">
		<cfif not structKeyExists(stTmp, idx)>
			<cfset stTmp[idx] = {}>
		</cfif>
		<cfset stTmp = stTmp[idx]>
		<cfset idx = listGetAt(arguments.sKey, lst, ".")>
	</cfloop>
	<cfset stTmp[idx] = arguments.value>
	<cfreturn arguments.st>
</cffunction>

<cffunction name="getAvailableLanguages" output="No" returntype="struct" 
	hint="Returns a struct where the key is the language code and the value is the language's name.">

	<cfdirectory name="local.qDir" directory="#getDirectoryFromPath(getCurrentTemplatePath())#/language/" action="list" mode="listnames" filter="*.xml">
	<cfset var result = {}>
	<cfloop query="qDir">

		<cffile action="read" file="#qDir.directory#/#qDir.name#" charset="UTF-8" variable="local.sContent">
		
		<cfset var xml  = XMLParse(sContent)>
		<cfset var lang = xml.language.XMLAttributes.Key>

		<cfset result[lang] = {

			name: xml.language.XMLAttributes.label
			,xml: xml
		}>
	</cfloop>
	<cfreturn result>
</cffunction>