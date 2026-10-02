component extends="org.lucee.cfml.test.LuceeTestCase" labels="serialize,component,metadata" {

	function run( testResults, testBox ) {
		describe( "LDEV-XXXX getMetadata() on a component restored with objectLoad( objectSave( cfc ) )", function() {

			it( "getMetadata() works on a fresh instance (control)", function() {
				var meta = getMetadata( newBean() );
				expect( meta.name ).toInclude( "Bean" );
			});

			it( "getMetadata() works on the restored instance", function() {
				var restored = objectLoad( objectSave( newBean() ) );
				expect( restored.hello() ).toBe( "hello test" );
				var meta = getMetadata( restored );
				expect( meta.name ).toInclude( "Bean" );
				expect( meta.functions.map( function( f ) { return f.name; } ) ).toInclude( "hello" );
			});

		});
	}

	private function newBean() {
		var bean = new LDEVXXXX.Bean();
		bean.setName( "test" );
		return bean;
	}
}
