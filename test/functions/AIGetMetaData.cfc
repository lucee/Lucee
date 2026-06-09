component extends="org.lucee.cfml.test.LuceeTestCase" {

	function skipAimock() {
		return structCount( server.getTestService( "aimock" ) ) == 0;
	}

	function run( testResults, testBox ) {
		describe( title="Test suite for AIGetMetaData()", skip=skipAimock, body=function() {

			it( title="returns metadata for the mock connection", body=function() {
				var meta = AIGetMetaData( "aimock" );
				expect( isStruct( meta ) ).toBeTrue();
			});

			it( title="includes model information when available", body=function() {
				var meta = AIGetMetaData( "aimock", true );
				expect( isStruct( meta ) ).toBeTrue();
				if ( structKeyExists( meta, "models" ) ) {
					expect( isQuery( meta.models ) || isArray( meta.models ) ).toBeTrue();
				}
			});

		});
	}

}
