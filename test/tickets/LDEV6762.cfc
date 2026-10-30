component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6762 sizeOf and the java agent / instrumentation are removed", function() {

			it( "the function sizeOf does not exist anymore", function() {
				expect( structKeyExists( getFunctionList(), "sizeOf" ) ).toBeFalse();
			} );

			it( "the loader API CFMLEngine does not offer getInstrumentation anymore", function() {
				var names = [];
				for ( var m in createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance().getClass().getMethods() ) {
					arrayAppend( names, m.getName() );
				}
				expect( names ).notToInclude( "getInstrumentation" );
				expect( names ).toInclude( "getSystemUtil" );
			} );

			it( "the instrumentation classes are gone", function() {
				expect( function() {
					createObject( "java", "lucee.runtime.instrumentation.InstrumentationFactory" );
				} ).toThrow();
			} );

		} );
	}

}
