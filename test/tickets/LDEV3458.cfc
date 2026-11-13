component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function run( testResults, testBox ) {
		describe( "LDEV-3458 cfcatch can be passed to java as java.lang.Throwable", function() {

			it( "cfcatch as Throwable argument of a java method (initCause)", function() {
				try {
					throw( message="LDEV3458 initCause" );
				}
				catch ( any e ) {
					var t = createObject( "java", "java.lang.Throwable" ).init( "wrapper" );
					t.initCause( e );
					expect( t.getCause().getMessage() ).toBe( "LDEV3458 initCause" );
				}
			});

			it( "cfcatch as Throwable argument of a java constructor", function() {
				try {
					throw( message="LDEV3458 constructor" );
				}
				catch ( any e ) {
					var ex = createObject( "java", "java.lang.RuntimeException" ).init( "wrapper", e );
					expect( ex.getCause().getMessage() ).toBe( "LDEV3458 constructor" );
				}
			});

			it( "cfcatch as Exception argument (java.lang.Exception)", function() {
				try {
					throw( message="LDEV3458 exception" );
				}
				catch ( any e ) {
					var ex = createObject( "java", "java.util.concurrent.ExecutionException" ).init( e );
					expect( ex.getCause().getMessage() ).toBe( "LDEV3458 exception" );
				}
			});

			it( "a caught java exception is passed as the original java exception", function() {
				try {
					createObject( "java", "java.lang.Integer" ).parseInt( "LDEV3458" );
				}
				catch ( any e ) {
					var ex = createObject( "java", "java.lang.RuntimeException" ).init( "wrapper", e );
					expect( ex.getCause().getClass().getName() ).toBe( "java.lang.NumberFormatException" );
				}
			});

			it( "cfcatch still behaves like a struct", function() {
				try {
					throw( message="LDEV3458 struct", type="LDEV3458" );
				}
				catch ( any e ) {
					expect( e.type ).toBe( "LDEV3458" );
					expect( isStruct( e ) ).toBeTrue();
					expect( structKeyExists( e, "tagContext" ) ).toBeTrue();
				}
			});
		});
	}
}
