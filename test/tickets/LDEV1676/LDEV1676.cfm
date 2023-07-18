<cfsavecontent variable="xml"><?xml version="1.0" encoding="ISO-8859-1"?>
	<cfif form.docType>
		<!DOCTYPE foo [
		<!ELEMENT foo ANY >
		<cfif FORM.entity>
			<!ENTITY xxe SYSTEM "http://update.lucee.org/rest/update/provider/echoGet/cgi" >
		</cfif>
		]>
	</cfif>
	<cfif form.entity>
		<foo>&xxe;</foo>
	<cfelse>
		<foo>lucee</foo>
	</cfif>
</cfsavecontent>
<cfscript>
	/*
	settings = getApplicationSettings();

	systemOutput( form.toJson(), true );
	if (structKeyExists(settings, "xmlFeatures" ) ) {
		systemOutput( settings.xmlFeatures.toJson(), true );
	} else {
		systemOutput("xmlFeatures not set", true);
	}
	systemOutput( "LDEV1676.cfc:" & CallStackGet( "array" )[ 2 ].linenumber, true );
	systemOutput( xml, true );
	*/
	try {
		result = xmlSearch( xml, "/foo" )[1].xmltext;
		//systemOutput( result, true );
		echo( result );
	} catch (e) {

		//systemOutput(cfcatch.type & " " & cfcatch.message, true);

		echo( cfcatch.type & " " & cfcatch.message );
	}
</cfscript>