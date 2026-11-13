component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title="LDEV-5135 invalid javasettings JSON in a component", body=function() {

			it( title="invalid JSON in a javasettings file names the component", body=function() {
				var msg = catchMessage( function() {
					new LDEV5135.BrokenJsonFile().test();
				} );
				expect( msg ).toInclude( "invalid javasettings in component" );
				expect( msg ).toInclude( "BrokenJsonFile.cfc" );
			} );

			it( title="the message also contains the original parse error and the JSON", body=function() {
				var msg = catchMessage( function() {
					new LDEV5135.BrokenJsonFile().test();
				} );
				expect( msg ).toInclude( "org.example:demo:1.0" );
				expect( msg ).toInclude( "Invalid Syntax" ); // the original parser error
			} );

			it( title="a javasettings file with valid JSON still works", body=function() {
				var cfc = new LDEV5135.ValidJsonFile();
				expect( cfc.test() ).toBe( "ok" );
			} );

			it( title="invalid inline JSON is still rejected when the component is compiled", body=function() {
				var msg = catchMessage( function() {
					new LDEV5135.BrokenInlineJson();
				} );
				expect( msg ).toInclude( "Invalid JSON in javasettings" );
			} );

		} );
	}

	private string function catchMessage( required function fn ) {
		try {
			arguments.fn();
		}
		catch ( any e ) {
			return e.message;
		}
		fail( "expected an exception for the invalid javasettings" );
	}

}
