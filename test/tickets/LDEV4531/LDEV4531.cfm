<cfscript>

	param name="FORM.scene" default="";

	if(FORM.scene eq 1) {
		result = A::usingLiteral;
		writeOutput( result.toString() )
	}

	if(FORM.scene eq 2) {
		result = A::usingDots;
		writeOutput( result.toString() )
	}

</cfscript>