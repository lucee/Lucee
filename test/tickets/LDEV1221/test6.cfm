<cfscript>
	param name="FORM.scene" default=1;
	dynamiVar = 'static';
	objInst = createObject( 'component', '#dynamiVar#' );

	if( form.scene EQ 1 ){
		try {

			data = objInst::getData();
			writeOutput(data.bar);
		} catch ( any e ) {
			writeOutput(e.message);	
		}
	} 
	else{
		
		try {

			data = objInst.getData();
			writeOutput(data.bar);
		} catch ( any e ) {
			writeOutput(e.message);	
		}
	} 
</cfscript>
