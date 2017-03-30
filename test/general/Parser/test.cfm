<cfparam name="form.scene" default="1">

<cfscript>
	if(form.scene EQ 1){
		try{
			writeOutput((['a','b','c'].3));
		} catch ( any e ){
			writeOutput(e.message);
		}
	}else if(form.scene EQ 2){
		try{
			tmpArray=['a','b','c'];
			writeOutput(tmpArray.3);
		} catch ( any e ){
			writeOutput(e.message);
		}
	}else if(form.scene EQ 3){
		try{
			writeOutput({'3':'C'}.3);
		} catch ( any e ){
			writeOutput(e.message);
		}
	}else if(form.scene EQ 4){
		try{
			tmpStr={'3':'C'};
			writeOutput(tmpStr.3);
		} catch ( any e ){
			writeOutput(e.message);
		}
	} else if(form.scene EQ 5){
		try{
			writeOutput(evaluate("['a','b','c'].3"));
		} catch ( any e ){
			writeOutput(e.message);
		}
	} else if(form.scene EQ 6){
		try{
			tmpArray=['a','b','c'];
			writeOutput(evaluate("tmpArray.3"));
		} catch ( any e ){
			writeOutput(e.message);
		}
	} else if(form.scene EQ 7){
		try{
			writeOutput(evaluate("{'3':'C'}.3"));
		} catch ( any e ){
			writeOutput(e.message);
		}
	} else if(form.scene EQ 8){
		try{
			tmpStr={'3':'C'};
			writeOutput(evaluate("tmpStr.3"));
		} catch ( any e ){
			writeOutput(e.message);
		}
	} else if(form.scene EQ 9){
		try{
			writeOutput(isValid('variableName','s'));
		} catch ( any e ){
			writeOutput(e.message);
		}
	} else if(form.scene EQ 10){
		try{
			writeOutput(isValid('variableName','s3'));
		} catch ( any e ){
			writeOutput(e.message);
		}
	} else if(form.scene EQ 11){
		try{
			writeOutput(isValid('variableName','3'));
		} catch ( any e ){
			writeOutput(e.message);
		}
	}
</cfscript>