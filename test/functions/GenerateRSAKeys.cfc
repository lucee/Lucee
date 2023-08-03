component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, textbox ) {
		describe( title = "Testcase for GenerateRSAKeys()", body = function() {
			
			it(title = "Checking defaults", body = function( currentSpec ) {
				var keys = GenerateRSAKeys();
				expect( keys ).toBeStruct().toHaveLength( 2 );
				expect( testKeys( keys ) ).toBeTrue();
			});

			it(title = "Checking with 1024", body = function( currentSpec ) {
				var keys = GenerateRSAKeys( 1024 );
				expect(keys).toBeStruct().toHaveLength( 2 );
				expect( testKeys( keys ) ).toBeTrue();
			});

			it(title = "Checking with 2048", body = function( currentSpec ) {
				var keys = GenerateRSAKeys( 2048 );
				expect( keys ).toBeStruct().toHaveLength( 2 );
				expect( testKeys( keys ) ).toBeTrue();
			});

			it(title = "Checking with 4096", body = function( currentSpec ) {
				var keys = GenerateRSAKeys( 4096 );
				expect(keys).toBeStruct().toHaveLength( 2 );
				expect( testKeys( keys ) ).toBeTrue();
			});

			it(title = "Checking with 777", body = function( currentSpec ) {
				var keys = GenerateRSAKeys( 777 );
				expect( keys ).toBeStruct().toHaveLength( 2 );

				expect( function(){
					testKeys( keys )
				}).toThrow();
			});

		});
	}

	private boolean function testKeys( required struct keys ){
		var raw = repeatString( "Lucee " & createUniqueID(), 1024 );
		var enc = encrypt( raw, arguments.keys.private, "rsa" );
		var dec = decrypt( enc, arguments.keys.public, "rsa" );
		return ( CompareNoCase( raw, dec ) eq 0 );
	}
}