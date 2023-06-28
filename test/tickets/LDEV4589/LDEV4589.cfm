<cfscript>
	param name = "FORM.scene" default = "";
	if (form.scene == 1) {
		try{
			value = 1;
			res = value.booleanFormat();
		}
		catch(any e){
			res = e.message;
		}
	}
	if (form.scene == 2) {
		try{
			res = "false".booleanFormat();
		}
		catch(any e) {
			res = e.message;
		}
	}
	if (form.scene == 3) {
		try{
			res = "123".booleanFormat();
		}
		catch(any e) {
			res = e.message;
		}
	}
	writeOutput(res);
</cfscript>