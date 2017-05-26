<cfparam name="FORM.Scene" default="1">
<cfxml variable="xmldoc">
<settings>
	<Setting>
		<login name="detail">MitrahSoft</login>
	</Setting>
</settings>
</cfxml>
<cffile action="read" file="#expandpath('test.xsl')#" variable="xmltrans">
<cfif FORM.Scene EQ 1>
	<cfscript>
	try{
		result = "false";
		XmlTransform(xmldoc, xmltrans);
	} catch( any e ){
		result = e.message;
	}
	</cfscript>
<cfelse>
	<cfscript>
	try{
		result = "false";
		XmlTransform(xmldoc, "");
	} catch( any e ){
		result = e.message;
	}
	</cfscript>
</cfif>
<cfoutput>#result#</cfoutput>

