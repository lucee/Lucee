<cfscript>
	param name = "form.scene" default = "";
	struct = { test1=1, test2=3, test3=2 };

	if( form.scene eq 1 ) {
		try {
			writeOutput( isNull(struct?.test4) );
		}
		catch(e) {
			writeOutput( e.stacktrace );
		}
	}

	if( form.scene eq 2 ) {
		try {
			writeOutput( isNull(struct?.test1) );
		}
		catch(e) {
			writeOutput( e.stacktrace );
		}
	}
</cfscript>