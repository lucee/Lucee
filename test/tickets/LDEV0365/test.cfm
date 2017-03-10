<cfscript>
	param name="FORM.Scene" default="1";
	if(FORM.Scene == 1){
		aNames = ["Saravana", "pothy", "mitrahsoft"];
		count = 0;
		aNames.map(function(){
			try {
				count++;
				abort
			} catch ( ANY e ){
				count = e.message;
			}
		});
		writeOutput(count);
	}

	if(FORM.Scene == 2){
		aNames = ["Saravana", "pothy", "mitrahsoft"];
		count = 0;
		aNames.map(function(){
			try {
				count++;
				writeOutput(count);
				abort;
			} catch ( ANY e ){
				count = e.message;
			}
		});
	}
</cfscript>