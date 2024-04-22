component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-3661", body=function() {

			it( title='check deserializeJSON 1',body=function( currentSpec ) {
				
				var myJSON = '{"lat":20.12283319000001}';
				var decoded = deserializeJSON( myJSON );
				expect( numberFormat( decoded.lat, "99.99999999999999" ) ).toBe( "20.12283319000001" )
			});

			it( title='check deserializeJSON 2',body=function( currentSpec ) {
				
				var myJSON = '{"lat":20.12283319000001}';
				var decoded = deserializeJSON( myJSON );
				expect( serializeJSON( decoded ) ).toBe( '{"lat":20.12283319000001}' );
				
			});

			it( title='check deserializeJSON 3',body=function( currentSpec ) {
				
				var myJSON = '{"lat":20.12283319000001}';
				var decoded = deserializeJSON( myJSON );
				expect( getMetadata(decoded.lat).name ).notToBe( "java.lang.String" );
				
			});

		});
	}

}
