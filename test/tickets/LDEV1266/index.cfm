<cfscript>
	param name="form.scene" default="1";
	path = ExpandPath( "./" );
	if( form.scene EQ 1){
		try{
			javaInstance = CreateObject( "java", "simple", "#path#test" );
			writeOutput(isObject(javaInstance));
		} catch ( any e ){
			writeOutput(e.message);
		}
	} else{
		javaInstance = CreateObject( "java", "simple", "simple.jar" );
		writeOutput(isObject(javaInstance));
	}
</cfscript>