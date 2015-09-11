<cfif structKeyExists(form, "newLangFile")>
	<cftry>
		<cffile action="UPLOAD" filefield="form.newLangFile" destination="#expandPath('resources/language/')#" nameconflict="ERROR">
		<cfcatch>
			<cfthrow message="#stText.overview.langAlreadyExists#">
		</cfcatch>
	</cftry>
	<cfset sFile = expandPath("resources/language/" & cffile.serverfile)>
	<cffile action="READ" file="#sFile#" variable="sContent">
	<cftry>
		<cfset sXML     = XMLParse(sContent)>
		<cfset sLang    = sXML.language.XMLAttributes.label>
		<cfset stInLang = GetFromXMLNode(sXML.XMLRoot.XMLChildren)>
		<cfcatch>
			<cfthrow message="#stText.overview.ErrorWhileReadingLangFile#">
		</cfcatch>
	</cftry>
	<!--- count the total elements to estimate a correct lang file --->
	<cfset iTotal = 0>
	<cfloop collection="#stText#" item="sKey">
		<cfif isStruct(stText[sKey])>
			<cfset iTotal += structCount(stText[sKey])>
		<cfelse>
			<cfset iTotal ++>
		</cfif>
	</cfloop>

	<cfset iNewKeys = 0>
	<cfloop collection="#stInLang#" item="sKey">
		<cfif structKeyExists(stText, sKey)>
			<cfif isStruct(stInLang[sKey])>
				<cfset iNewKeys += structCount(stInLang[sKey])>
			<cfelse>
				<cfset iNewKeys ++>
			</cfif>
		</cfif>
	</cfloop>
	<cfif iNewKeys / iTotal lt 0.8>
		<cfthrow message="#stText.overview.LangFileSeemsIncomplete#">
	</cfif>
	<cfset sOut = Replace(stText.overview.LangFileUploaded, "[%]", NumberFormat(iNewKeys / iTotal * 100, "999") & "%", "ALL")>
	<cfset sOut = Replace(sOut, "[lang]", "<b>" & sLang & "</b>", "ALL")>
	<cfoutput>#sOut#</cfoutput>
	<cfadmin	action="updateContext"
	            type="server"
	            password="#session["password"&request.adminType]#"
	            source="#sFile#"
	            destination="admin/resources/language/#cffile.serverfile#">
</cfif>

<cffunction name="GetFromXMLNode" returntype="any" output="No">
	<cfargument name="stXML" required="Yes">
	<cfset var el        = "">
	<cfset var stRet = {}>
	<cfloop array="#arguments.stXML#" index="el">
		<cfset setStructElement(stRet, el.XMLAttributes.key, el.XMLText)>
	</cfloop>
	<cfreturn stRet>
</cffunction>
