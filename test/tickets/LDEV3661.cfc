component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-3661", body=function() {

			it( title='check deserializeJSON',body=function( currentSpec ) {
				
				var myJSON = '{"lat":20.12283319000001}';
				var decoded = deserializeJSON( myJSON );
				expect( numberFormat( decoded.lat, "99.99999999999999" ) ).toBe( "20.12283319000001" )
				// FUTURE Lucee 6 expect( serializeJSON( decoded ) ).toBe( myJSON );
				// FUTURE Lucee 6 expect( getMetadata(decoded.lat).name ).toBe( "java.lang.Double" );
				
			});

		});
	}

}
