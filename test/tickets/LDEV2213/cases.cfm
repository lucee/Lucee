<cfscript>
	function doSomething( any onComplete ) {
		onComplete();
	}

	// original ticket case: closure updates a var of the enclosing function
	function enclosing() {
		var myVar = "";
		var before = myVar;
		doSomething( function() {
			myVar = "testcase";
		} );
		return "Before: [#before#] After: [#myVar#]";
	}

	// closure in a closure updates a var of the outermost function
	function nested() {
		var counter = 1;
		doSomething( function() {
			doSomething( function() {
				counter = 2;
			} );
		} );
		return "counter: #counter#";
	}

	// Adam Cameron's case: variables[ key ] inside a closure writes the page's variables scope
	function adam() {
		var one = "tahi";
		var red = "whero";
		setInVariablesScope( { one: one, red: red } );
		return ( variables.keyExists( "one" ) ? variables.one : "one not set" ) & "," & ( variables.keyExists( "red" ) ? variables.red : "red not set" );
	}
	function setInVariablesScope( locals ) {
		locals.each( ( key, value ) => {
			variables[ key ] = value;
		} );
	}

	// a new, unknown name assigned in a closure
	function newName() {
		var result = "";
		doSomething( function() {
			freshName = "x";
		} );
		return "enclosing local: #local.keyExists( 'freshName' )#, variables: #variables.keyExists( 'freshName' )#";
	}

	switch ( url.case ) {
		case "enclosing": writeOutput( enclosing() ); break;
		case "nested": writeOutput( nested() ); break;
		case "adam": writeOutput( adam() ); break;
		case "newName": writeOutput( newName() ); break;
	}
</cfscript>
