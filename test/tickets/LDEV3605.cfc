component extends="org.lucee.cfml.test.LuceeTestCase" labels="component" {

	function run( testResults, testBox ) {
		describe( title="LDEV-3605 onMissingMethod without declared arguments", body=function( currentSpec ) {

			it( title="arguments are named missingMethodName and missingMethodArguments", body=function( currentSpec ) {
				var res = new LDEV3605.NoArgs().foo();
				expect( structKeyExists( res, "missingMethodName" ) ).toBeTrue();
				expect( structKeyExists( res, "missingMethodArguments" ) ).toBeTrue();
				expect( res.missingMethodName ).toBe( "foo" );
				expect( structCount( res.missingMethodArguments ) ).toBe( 0 );
			});

			it( title="positional access still works", body=function( currentSpec ) {
				var res = new LDEV3605.NoArgs().bar( "a", "b" );
				expect( res[ 1 ] ).toBe( "bar" );
				expect( res[ 2 ][ 1 ] ).toBe( "a" );
				expect( res[ 2 ][ 2 ] ).toBe( "b" );
				expect( res.missingMethodArguments[ 2 ] ).toBe( "b" );
			});

			it( title="named arguments of the missing method", body=function( currentSpec ) {
				var res = new LDEV3605.NoArgs().baz( x=1, y=2 );
				expect( res.missingMethodName ).toBe( "baz" );
				expect( res.missingMethodArguments.x ).toBe( 1 );
				expect( res.missingMethodArguments.y ).toBe( 2 );
			});

			it( title="declared arguments keep their own names", body=function( currentSpec ) {
				var res = new LDEV3605.Declared().foo( "a" );
				expect( res.name ).toBe( "foo" );
				expect( res.args[ 1 ] ).toBe( "a" );
				expect( listSort( res.keys, "textnocase" ) ).toBe( "ARGS,NAME" );
			});
		});
	}
}
