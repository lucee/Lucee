<cfscript>
	param name="FORM.scene" default=1;
	obj = new test();
	if(FORM.scene == 1){
		try{ 
			writeOutput(obj.testA()); 
		} catch( any e ){ 
			writeOutput(e.message ); 
		}
	} else{
		try{ 
			writeOutput( obj.testB()); 
		} catch( any e ){ 
			writeOutput(e.message); 
		}
	}

</cfscript>