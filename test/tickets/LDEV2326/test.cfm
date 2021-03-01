<cfscript>
	param name = "form.scene" default = "";

    sourceDir = expandPath('./pdf');

	if( form.scene eq 1 ) {
	    try {
	    	cfpdf( action = 'merge', directory = sourceDir, destination = expandPath( './mergedOne.pdf'), overwrite = false );
	    	writeOutput(fileexists(expandPath('./mergedone.pdf')));
	    }
	    catch(e) {
	    	writeOutput(e.message);
	    }
	}
	if( form.scene eq 2 ) {
	    try {
	    	cfpdf( action = 'merge', directory = sourceDir, destination = expandPath( './mergedTwo.pdf'), overwrite = true );
	    	writeOutput(fileexists(expandPath('./mergedTwo.pdf')));
	    }
	    catch(e) {
	    	writeOutput(e.message);
	    }
	}
</cfscript>