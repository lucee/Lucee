<cfscript>
	param name="FORM.Scene" default="1_1";
	testobj = new test();
	if(listFirst(FORM.Scene, "_") == 1){
		testobj.container(1, listLast(FORM.Scene, "_"));
	} else{
		testobj.subFunction(1, listLast(FORM.Scene, "_"));
	}
</cfscript>
