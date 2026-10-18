<cfscript>
	param name="form.scene" default="";
	if ( form.scene == "undefined" ) {
		result = ldev5343_undefined ?? "lucee";
	}
	else if ( form.scene == "value" ) {
		value = "CFML";
		result = value ?? "lucee";
	}
	else {
		value = null;
		result = value ?? "lucee";
	}
	writeOutput( result );
</cfscript>
