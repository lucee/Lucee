<cfscript>
	param name="form.scene" default="";

	shouldShowDefaultFrom = new test();
	overrideDefaultFrom = new test();

	if( form.scene eq 1 ){
		showDefault = shouldShowDefaultFrom.getFrom();
		try{
			if(showDefault eq '[default]'){
				writeOutput(showDefault);
			}
		}
		catch(any e){
			writeOutput(e.message);
		}
	}

	if( form.scene eq 2 ){
		overrideDefault = overrideDefaultFrom.setFrom("hello world!");
		writeOutput(overrideDefaultFrom.getFrom());
	}
</cfscript>