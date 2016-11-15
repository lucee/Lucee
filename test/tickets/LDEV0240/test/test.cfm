<cfscript>
	setting enablecfoutputonly="true";
	param name="FORM.Scene" default="1";
	try {
		if(FORM.Scene == 1)
			Foo = new LDEV0240.comp1();
		else{
			Foo = new comp3();
			baseObj = Foo.foo();
		}
	}
	catch(any e) {
		writeOutput(e.Message);
	}
</cfscript>