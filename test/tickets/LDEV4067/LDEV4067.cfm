<cfparam name="FORM.scene" default="">
<cfscript>
	if(form.scene == 1) {
		newPerson = new test();
		newPerson.setName( "Michael" );
		writeoutput( newPerson.testClosureThis.MAPPERS.THENAME());
	} 
	else if(form.scene == 2) {
		newPerson = new test();
		newPerson.setName( "Michael" );
		writeoutput( newPerson.testClosureVar.MAPPERS.THENAME());
	} 
	else if(form.scene == 3) {
		newPerson = new test();
		newPerson.setName("Michael");
		writeoutput( newPerson.testLambdaThis.MAPPERS.THENAME());
	} 
	else if(form.scene == 4) {
		newPerson = new test();
		newPerson.setName("Michael");
		writeoutput( newPerson.testLambdaVar.MAPPERS.THENAME());
	}
	else if(form.scene == 5) {
		newPerson = entityNew("test");
		newPerson.setName( "Michael" );
		writeoutput( newPerson.testClosureThis.MAPPERS.THENAME());
	} 
	else if(form.scene == 6) {
		newPerson = entityNew("test");
		newPerson.setName( "Michael" );
		writeoutput( newPerson.testClosureVar.MAPPERS.THENAME());
	} 
	else if(form.scene == 7) {
		newPerson = entityNew("test");
		newPerson.setName("Michael");
		writeoutput( newPerson.testLambdaThis.MAPPERS.THENAME());
	} 
	else if(form.scene == 8) {
		newPerson = entityNew("test");
		newPerson.setName("Michael");
		writeoutput( newPerson.testLambdaVar.MAPPERS.THENAME());
	}
</cfscript>