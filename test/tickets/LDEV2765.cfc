component extends="org.lucee.cfml.test.LuceeTestCase" labels="soap" {

	function run( testResults, testBox ) {
		describe( "LDEV-2765 getSoapRequest without a webservice object", function() {

			it( title="throws a proper exception instead of a NullPointerException", body=function() {
				var type = "";
				var msg = "";
				try {
					getSoapRequest();
				}
				catch ( any e ) {
					type = e.type;
					msg = e.message;
				}
				expect( type ).notToBe( "" );
				expect( type ).notToBe( "java.lang.NullPointerException" );
				expect( msg ).toInclude( "webservice" );
			});

			it( title="throws a proper exception for an object that is not a webservice", body=function() {
				var msg = "";
				try {
					getSoapRequest( {} );
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toInclude( "webservice" );
			});

		});
	}

}
