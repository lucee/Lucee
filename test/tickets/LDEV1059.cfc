component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults, testBox ) {
		describe( "LDEV-1059 a query column in the caller must not hide a function in the variables scope of a CFC", function() {

			it( title="CFC created with new, positional arguments", body=function( currentSpec ) {
				var c = new LDEV1059.content();
				var q = queryNew( "cachable", "varchar", [ [ "x" ] ] );
				var result = "";
				loop query="q" {
					result = c.foo();
				}
				expect( result ).toBe( "called" );
			});

			it( title="CFC created with createObject, positional arguments", body=function( currentSpec ) {
				var c = createObject( "component", "LDEV1059.content" );
				var q = queryNew( "cachable", "varchar", [ [ "x" ] ] );
				var result = "";
				loop query="q" {
					result = c.foo();
				}
				expect( result ).toBe( "called" );
			});

			it( title="named arguments", body=function( currentSpec ) {
				var c = new LDEV1059.content();
				var q = queryNew( "cachable", "varchar", [ [ "x" ] ] );
				var result = "";
				loop query="q" {
					result = c.fooNamed();
				}
				expect( result ).toBe( "named" );
			});

			it( title="still works without a query in the caller", body=function( currentSpec ) {
				var c = new LDEV1059.content();
				expect( c.foo() ).toBe( "called" );
			});

		});
	}

}
