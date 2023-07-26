<cfsavecontent variable="xml"><?xml version="1.0" encoding="ISO-8859-1"?>
	<cfif form.docType>
		<!DOCTYPE hibernate-mapping PUBLIC "-//Hibernate/Hibernate Mapping DTD 3.0//EN" "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd">
	</cfif>
	<hibernate-mapping>lucee</hibernate-mapping>
</cfsavecontent>
<cfscript>
	
	if (form.cfapplicationOverride){
		param name="form.cfapplicationOverrideState";
		//systemOutput("cfapplicationOverride", true)
		application action="update" xmlFeatures={
			"externalGeneralEntities": true,
			"secure": false,
			"http://apache.org/xml/features/disallow-doctype-decl": form.cfapplicationOverrideState
		};
	}
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
		if ( form.xmlParseThenSearch )
			xml = xmlParse (xml );
		result = xmlSearch( xml, "/hibernate-mapping" )[1].xmltext;
		//systemOutput( result, true );
		echo( result );
	} catch (e) {
		//systemOutput(cfcatch.type & " " & cfcatch.message, true);
		echo( cfcatch.type & " " & cfcatch.message );
	}
</cfscript>