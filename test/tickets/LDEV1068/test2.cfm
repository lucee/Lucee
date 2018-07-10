<cfscript>
	processingdirective preserveCase="false";
	param name="FORM.Scene" default="1";
	empDetails = queryNew("name,age,sex","varchar,integer,varchar", [['saravana',35,'male'],['Bob',20, 'female'],['pothy',25, 'male']]);
	if(FORM.Scene == 1){
		result = serializeJSON(empDetails,true);
	}else if(FORM.Scene == 2){
		wddx action="cfml2wddx" input=empDetails output="wddx";
		result = find("fieldNames='NAME,AGE,SEX'", wddx) > 0
	}else{
		result = find("NAME,AGE,SEX", empDetails.columnlist) > 0;
	}
	writeOutput(result);
</cfscript>