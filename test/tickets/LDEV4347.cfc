component extends="org.lucee.cfml.test.LuceeTestCase" labels="loop" {
	function run( testResults, textbox ) {
		describe( "Testcase for LDEV-4347", function() {
			it( title="loop struct=application deleting keys of the same scope", body=function( currentSpec ) {
				application name="test-4347" ;
				application[ "simple4347" ] = "test";
				application[ "array4347" ] = [];
				application[ "struct4347" ] = {};
				var res = "successfully deleted";
				try {
					loop struct=application index="local.k" item="local.v" {
						if ( !isSimpleValue( v ) ) structDelete( application, k );
					}
				}
				catch ( any e ) { res = e.message; }
				expect( res ).toBe( "successfully deleted" );
			});
			it( title="loop struct= deleting keys of a regular struct", body=function( currentSpec ) {
				var s = { a: 1, b: 2, c: 3, d: 4 };
				var res = "successfully deleted";
				try {
					loop struct=s index="local.k" item="local.v" { structDelete( s, k ); }
				}
				catch ( any e ) { res = e.message; }
				expect( res ).toBe( "successfully deleted" );
				expect( structCount( s ) ).toBe( 0 );
			});
			it( title="for ( k in struct ) deleting keys of a regular struct", body=function( currentSpec ) {
				var s = { a: 1, b: 2, c: 3, d: 4 };
				var res = "successfully deleted";
				try {
					for ( var k in s ) structDelete( s, k );
				}
				catch ( any e ) { res = e.message; }
				expect( res ).toBe( "successfully deleted" );
			});
			it( title="structEach deleting keys of a regular struct", body=function( currentSpec ) {
				var s = { a: 1, b: 2, c: 3, d: 4 };
				var res = "successfully deleted";
				try {
					structEach( s, function( k ) { structDelete( s, k ); } );
				}
				catch ( any e ) { res = e.message; }
				expect( res ).toBe( "successfully deleted" );
			});
		});
	}
}
