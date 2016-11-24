<cfscript>
	setting enablecfoutputonly="true";
	param name="FORM.Scene" default="1";
	try {
		if(FORM.Scene == 1)
			baseObj = new comp1();
		else{
			Foo = new comp2();
			baseObj = Foo.foo();
		}
		writeOutput(baseObj.CompName);
	}
	catch(any e) {
		writeOutput(e.Message);
	}
</cfscript>