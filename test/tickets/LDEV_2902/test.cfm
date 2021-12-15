<cfscript>
	param name = "FORM.scene" default = "";
	if (form.scene == 1){
		writeOutput(getApplicationSettings().datasources.testTimeZone.timezone);
	}
	if (form.scene == 2){
		try{
			writeOutput(getApplicationSettings().datasources.testNoTimeZone.timezone);
		}
		catch(any e){
			writeOutput(e.message);
		}
	}
	if (form.scene == 3){
		try{
			writeOutput(getApplicationSettings().datasources.testemptyTimeZone.timezone);
		}
		catch(any e){
			writeOutput(e.message);
		}
	}
</cfscript>
