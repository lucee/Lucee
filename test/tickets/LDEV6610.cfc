component extends="org.lucee.cfml.test.LuceeTestCase" {

	function afterAll() {
		application action="update" nullSupport=false;
	}

	function run( testResults, testBox ) {

		describe( title="LDEV-6610 nullSupport=true: typed argument with a null default, not passed", body=function() {

			beforeEach( function( currentSpec, data ) {
				application action="update" nullSupport=true;
			});

			afterEach( function( currentSpec, data ) {
				application action="update" nullSupport=false;
			});

			it( title="query, positional call", body=function() {
				var args = _query();
				expect( args ).toHaveKey( "q" );
				expect( isNull( args.q ) ).toBeTrue();
			});

			it( title="query, named call", body=function() {
				var args = _query( other="x" );
				expect( args ).toHaveKey( "q" );
				expect( isNull( args.q ) ).toBeTrue();
			});

			it( title="query, via argumentCollection", body=function() {
				var args = _proxyQuery();
				expect( args ).toHaveKey( "q" );
				expect( isNull( args.q ) ).toBeTrue();
			});

			it( title="query, argumentCollection without the key", body=function() {
				var args = _query( argumentCollection={ other: "x" } );
				expect( args ).toHaveKey( "q" );
				expect( isNull( args.q ) ).toBeTrue();
			});

			it( title="struct, array, boolean, date, component", body=function() {
				var args = _otherTypes();
				for ( var k in [ "s", "a", "b", "d", "c" ] ) {
					expect( args ).toHaveKey( k );
					expect( isNull( args[ k ] ) ).toBeTrue( k );
				}
			});

			it( title="tag syntax cfargument default=nullValue()", body=function() {
				var args = new LDEV6610.TagArgs().tagQuery();
				expect( args ).toHaveKey( "q" );
				expect( isNull( args.q ) ).toBeTrue();
			});

			it( title="a passed value is still type checked", body=function() {
				expect( function() {
					_query( "not a query" );
				} ).toThrow();
			});

			it( title="a passed query is used", body=function() {
				var args = _query( queryNew( "a" ) );
				expect( isQuery( args.q ) ).toBeTrue();
			});

			it( title="a non-null default is still type checked", body=function() {
				expect( function() {
					_badDefault();
				} ).toThrow();
			});
		});

		describe( title="LDEV-6610 nullSupport=false (control)", body=function() {

			beforeEach( function( currentSpec, data ) {
				application action="update" nullSupport=false;
			});

			it( title="query, not passed, null default: argument not defined", body=function() {
				var args = _query();
				expect( isNull( args.q ) ).toBeTrue();
			});
		});
	}

	private function _query( query q=nullValue(), other ) {
		return arguments;
	}

	private function _proxyQuery( query q, other ) {
		return _query( argumentCollection=arguments );
	}

	private function _otherTypes( struct s=nullValue(), array a=nullValue(), boolean b=nullValue(), date d=nullValue(), component c=nullValue() ) {
		return arguments;
	}

	private function _badDefault( query q="not a query" ) {
		return arguments;
	}

}
