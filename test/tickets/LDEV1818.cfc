component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1818", body=function() {
			it(title = "Checking Interface Methods while doesn't use semicolon", body = function( currentSpec ) {
				try{
					var instance = new 'LDEV1818.Sample'();
					local.result = instance.name("lucee");
				} catch( any e ){
					local.result = e.Message ;
				}
				expect(local.result).toBe('lucee');
			});

			it(title = "Interface with several methods, with and without semicolon", body = function( currentSpec ) {
				var instance = new LDEV1818.Sample2();
				expect( instance.create() ).toBe( 1 );
				expect( instance.exists( "lucee" ) ).toBeTrue();
				var meta = getComponentMetadata( "LDEV1818.test2" );
				expect( arrayLen( meta.functions ) ).toBe( 3 );
			});
		});
	}
}
