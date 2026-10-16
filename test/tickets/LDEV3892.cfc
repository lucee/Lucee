component extends="org.lucee.cfml.test.LuceeTestCase" labels="exception" {

	function run( testResults, testBox ) {
		describe( "LDEV-3892: Improve exception messaging for complex object type casting errors", function() {

			it( "should include scope name when casting URL scope to string", function() {
				// BEFORE: "Can't cast Complex Object Type Struct to String"
				// AFTER:  "Can't cast Complex Object Type [URL scope] to String"
				// When you assign url="...", it goes to variables.url, but referencing url gives you the URL scope
				try {
					url = "http://localhost:7888";
					cfhttp( url=url ); // tries to pass URL scope struct instead of the string
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "URL" );
					expect( e.message ).toInclude( "scope" );
					expect( e.message ).toInclude( "Can't cast" );
				}
			});

			it( "should include scope name when casting FORM scope to string", function() {
				// BEFORE: "Can't cast Complex Object Type Struct to String"
				// AFTER:  "Can't cast Complex Object Type [FORM scope] to String"
				try {
					form = "test";
					var result = "value: " & form; // string concatenation tries to cast FORM scope to string
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "FORM" );
					expect( e.message ).toInclude( "scope" );
				}
			});

			it( "should provide helpful detail message for scope conflicts", function() {
				// BEFORE detail: "Use Built-In-Function \"serialize(Struct):String\" to create a String from Struct"
				// AFTER detail:  "Variable name conflicts with [APPLICATION scope]. Use [variables.application] to access your variable, or rename it."
				try {
					application = "myapp";
					var result = "app: " & application;
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.detail ).toInclude( "scope" );
					expect( e.detail ).toInclude( "variables." );
				}
			});

			it( "should still work for non-scope struct casting", function() {
				// Regular structs get [Struct] but NO scope warning
				// BEFORE: "Can't cast Complex Object Type Struct to String"
				// AFTER:  "Can't cast Complex Object Type [Struct] to String"
				try {
					var myStruct = { foo: "bar" };
					var result = "struct: " & myStruct;
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "Can't cast" );
					// Should NOT mention scope since it's not a scope
					expect( e.message ).notToInclude( "scope" );
				}
			});

			it( "should handle CGI scope", function() {
				// BEFORE: "Can't cast Complex Object Type Struct to String"
				// AFTER:  "Can't cast Complex Object Type [CGI scope] to String"
				try {
					cgi = "test";
					var result = "cgi: " & cgi;
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "CGI" );
					expect( e.message ).toInclude( "scope" );
				}
			});

			it( "should handle SERVER scope", function() {
				// BEFORE: "Can't cast Complex Object Type Struct to String"
				// AFTER:  "Can't cast Complex Object Type [SERVER scope] to String"
				try {
					server = "test";
					var result = "server: " & server;
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "SERVER" );
					expect( e.message ).toInclude( "scope" );
				}
			});

			it( "should handle COOKIE scope", function() {
				// BEFORE: "Can't cast Complex Object Type Struct to String"
				// AFTER:  "Can't cast Complex Object Type [COOKIE scope] to String"
				try {
					cookie = "test";
					var result = "cookie: " & cookie;
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "COOKIE" );
					expect( e.message ).toInclude( "scope" );
				}
			});

			it( "should handle LOCAL scope in function", function() {
				// BEFORE: "Can't cast Complex Object Type Struct to String"
				// AFTER:  "Can't cast Complex Object Type [LOCAL scope] to String"
				function testLocalScope() {
					local = "test";
					var result = "local: " & local; // tries to cast LOCAL scope to string
				}
				try {
					testLocalScope();
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "LOCAL" );
					expect( e.message ).toInclude( "scope" );
				}
			});

			it( "should handle casting to boolean", function() {
				// BEFORE: "can't cast Complex Object Type Struct to a boolean value"
				// AFTER:  "can't cast Complex Object Type [URL scope] to a boolean value"
				try {
					url = "test";
					if ( url ) {
						// will try to cast URL scope to boolean
					}
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "URL" );
					expect( e.message ).toInclude( "scope" );
					expect( e.message ).toInclude( "boolean" );
				}
			});

			it( "should handle casting to number", function() {
				// BEFORE: "can't cast Complex Object Type Struct to a number value"
				// AFTER:  "can't cast Complex Object Type [FORM scope] to a number value"
				try {
					form = "test";
					var total = form + 10; // arithmetic tries to cast FORM scope to number
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "FORM" );
					expect( e.message ).toInclude( "scope" );
					expect( e.message ).toInclude( "number" );
				}
			});

			it( "should handle Array casting errors", function() {
				try {
					var myArray = [ 1, 2, 3 ];
					var result = "array: " & myArray;
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "Can't cast" );
					expect( e.message ).toInclude( "Array" );
					// Should NOT mention scope
					expect( e.message ).notToInclude( "scope" );
				}
			});

			it( "should handle Query casting errors", function() {
				try {
					var qry = queryNew( "id,name", "integer,varchar", [ [ 1, "test" ] ] );
					var result = "query: " & qry;
					fail( "Expected ExpressionException to be thrown" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "expression" );
					expect( e.message ).toInclude( "Can't cast" );
					expect( e.message ).toInclude( "Query" );
					// Should NOT mention scope
					expect( e.message ).notToInclude( "scope" );
				}
			});

		});
	}

}
