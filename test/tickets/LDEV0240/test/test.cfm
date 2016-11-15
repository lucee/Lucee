<cfscript>
	setting enablecfoutputonly="true";
	param name="FORM.Scene" default="1";
	try {
		if(FORM.Scene == 1)
			baseObj = new LDEV0240.comp1();
		else{
			Foo = new comp3();
			baseObj = Foo.foo();
		}
	}
	catch(any e) {
		writeOutput("Error Occurred!!!");
	}
</cfscript>