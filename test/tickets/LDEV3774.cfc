component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {
	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-3774", function() {
			it( title="queryNew() using struct as data and struct key has space", body=function( currentSpec ){
				var sct = { "foo": "foo", " bar ": "bar" };
				var qry = queryNew( "foo,bar", "varchar,varchar", sct );
				expect( qry.foo ).toBe( "foo" );
				expect( qry.bar ).toBe( "bar" );
			});
			it( title="queryNew() using array of struct as data and struct key has space", body=function( currentSpec ){
				var arr = [ { "foo": "foo", " bar ": "bar" } ];
				var qry = queryNew( "foo,bar", "varchar,varchar", arr );
				expect( qry.foo ).toBe( "foo" );
				expect( qry.bar ).toBe( "bar" );
			});
			it( title="queryNew() an exact key wins over a key with spaces", body=function( currentSpec ){
				var qry = queryNew( "bar", "varchar", [ { " bar ": "spaced", "bar": "exact" } ] );
				expect( qry.bar ).toBe( "exact" );
				qry = queryNew( "bar", "varchar", { " bar ": [ "spaced" ], "bar": [ "exact" ] } );
				expect( qry.recordCount ).toBe( 1 );
				expect( qry.bar ).toBe( "exact" );
			});
		});
	}
}
