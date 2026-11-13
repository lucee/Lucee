component extends="org.lucee.cfml.test.LuceeTestCase" labels="null" {

	function afterAll() {
		application action="update" nullSupport=false;
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3246 cfparam default = null with full null support", function() {

			it( "cfparam with a null default defines the variable as null", function() {
				application action="update" nullSupport=true;
				var res = "";
				try {
					param name="variables.ldev3246" default=nullValue() type="string";
					res = "ok:" & ( isNull( variables.ldev3246 ) ? "null" : "[" & variables.ldev3246 & "]" );
				}
				catch ( any e ) {
					res = "error:" & e.message;
				}
				finally {
					application action="update" nullSupport=false;
				}
				expect( res ).toBe( "ok:null" );
			});

			it( "cfparam with a null default from a query column", function() {
				application action="update" nullSupport=true;
				var res = "";
				try {
					var qry = queryNew( "col", "varchar", [ [ nullValue() ] ] );
					param name="variables.ldev3246b" default=qry.col[ 1 ] type="string";
					res = "ok:" & ( isNull( variables.ldev3246b ) ? "null" : "[" & variables.ldev3246b & "]" );
				}
				catch ( any e ) {
					res = "error:" & e.message;
				}
				finally {
					application action="update" nullSupport=false;
				}
				expect( res ).toBe( "ok:null" );
			});
					it( "without full null support a null default still throws (unchanged)", function() {
				application action="update" nullSupport=false;
				var res = "";
				try {
					param name="variables.ldev3246c" default=nullValue() type="string";
					res = "ok";
				}
				catch ( any e ) {
					res = "error:" & e.message;
				}
				expect( res ).toInclude( "was not provided" );
			});

			it( "with full null support an existing variable is still validated", function() {
				application action="update" nullSupport=true;
				var res = "";
				try {
					variables.ldev3246d = "abc";
					param name="variables.ldev3246d" default=nullValue() type="numeric";
					res = "ok";
				}
				catch ( any e ) {
					res = "error";
				}
				finally {
					application action="update" nullSupport=false;
				}
				expect( res ).toBe( "error" );
			});
		});
	}
}
