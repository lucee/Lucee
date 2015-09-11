<cfinclude template="../resources/resources.cfm">

<cfoutput>
<cfsavecontent variable="content">
#chr(60)#cfprocessingdirective pageencoding="utf-8">#chr(60)#cfscript>
if(not StructKeyExists(session,'lucee_admin_lang'))session.lucee_admin_lang='en';
if(session.lucee_admin_lang EQ 'de') {
	stText=#serialize(stText['de'])#;
}
else {
	stText=#serialize(stText['en'])#;
}
#chr(60)#/cfscript>

</cfsavecontent>
</cfoutput>
<cffile action="write" file="../resources/text.cfm" output="#trim(content)#" addnewline="yes" charset="UTF-8">
<!--- 
<cfloop collection="#stText#" item="key">
	<cfoutput>
	<cfsavecontent variable="content">
	#chr(60)#cfscript>
	stText=#serialize(stText[key])#;
	#chr(60)#/cfscript>
	</cfsavecontent>
	<cffile action="write" file="../resources/text_#key#.cfm" output="#trim(content)#" addnewline="no">
	</cfoutput>
</cfloop> --->