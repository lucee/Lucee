<cfscript>
	param name="FORM.Scene" default="1";
	try {
		if( FORM.Scene == 1 ){
			c1 = new comp1();
			c2 = createObject("component", "comp1");
		}else{
			c1 = createObject("component", "comp2");
			c2 = new comp2();
		}
		c1.foo();
		silent {dump(c1);}
		c2.foo();
		silent {dump(c2);}
	}
	catch(any e) {
		if( e.Message != -1 )
			writeoutput(e.message);
		else
			writeOutput(e.Type);
	}
</cfscript>