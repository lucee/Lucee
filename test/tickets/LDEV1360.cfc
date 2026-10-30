component extends="org.lucee.cfml.test.LuceeTestCase" labels="cachedWithin" {

	function run( testResults, testBox ) {
		describe( "LDEV-1360 only one call per cache key executes the function on a cold cache", function() {

			it( title="concurrent calls with the same arguments execute the function once", body=function( currentSpec ) {
				var c = new LDEV1360.Cached();
				var id = createUUID();
				var results = _parallel( c, "slow", id, 5 );
				for ( var i = 1; i <= 5; i++ ) {
					expect( results[ i ].error ).toBe( "" );
					expect( results[ i ].value ).toBe( "value-#id#" );
					expect( results[ i ].output ).toBe( "out-#id#" );
				}
				expect( c.getCount( id ) ).toBe( 1 );
			});

			it( title="a call after the cache is filled does not execute the function", body=function( currentSpec ) {
				var c = new LDEV1360.Cached();
				var id = createUUID();
				c.slow( id );
				c.slow( id );
				expect( c.getCount( id ) ).toBe( 1 );
			});

			it( title="different arguments do not block each other", body=function( currentSpec ) {
				var c = new LDEV1360.Cached();
				var id1 = createUUID();
				var id2 = createUUID();
				var start = getTickCount();
				thread name="ldev1360a#id1#" c=c id=id1 { attributes.c.slow( attributes.id ); }
				thread name="ldev1360b#id2#" c=c id=id2 { attributes.c.slow( attributes.id ); }
				thread action="join" name="ldev1360a#id1#,ldev1360b#id2#" timeout="20000";
				expect( c.getCount( id1 ) ).toBe( 1 );
				expect( c.getCount( id2 ) ).toBe( 1 );
				// both executed in parallel, not one after the other
				expect( getTickCount() - start ).toBeLT( 1600 );
			});

			it( title="if the first call throws, the other calls do not hang and get a result", body=function( currentSpec ) {
				var c = new LDEV1360.Cached();
				var id = createUUID();
				var start = getTickCount();
				var results = _parallel( c, "failFirst", id, 4 );
				expect( getTickCount() - start ).toBeLT( 10000 );
				var errors = 0;
				var ok = 0;
				for ( var i = 1; i <= 4; i++ ) {
					if ( len( results[ i ].error ) ) errors++;
					else {
						ok++;
						expect( results[ i ].value ).toBe( "value-#id#" );
					}
				}
				expect( errors ).toBe( 1 );
				expect( ok ).toBe( 3 );
			});

			it( title="a function calling itself with the same arguments does not wait for itself", body=function( currentSpec ) {
				// the function only returns once the recursion ends, so use a different argument to end it
				var c = new LDEV1360.Cached();
				expect( c.recursive( 0 ) ).toBe( "end" );
			});
		});
	}

	private array function _parallel( required any c, required string method, required string id, required numeric count ) {
		var names = [];
		var token = createUUID();
		for ( var i = 1; i <= arguments.count; i++ ) {
			var name = "ldev1360t#i##token#";
			arrayAppend( names, name );
			thread name=name c=arguments.c method=arguments.method id=arguments.id {
				thread.val = "";
				thread.err = "";
				thread.out = "";
				try {
					var rtn = "";
					savecontent variable="local.o" {
						rtn = invoke( attributes.c, attributes.method, [ attributes.id ] );
					}
					thread.val = rtn;
					thread.out = local.o;
				}
				catch ( any e ) {
					thread.err = e.message;
				}
			}
		}
		thread action="join" name=arrayToList( names ) timeout="30000";
		var results = [];
		for ( var name in names ) {
			arrayAppend( results, { value: cfthread[ name ].val, error: cfthread[ name ].err, output: cfthread[ name ].out } );
		}
		return results;
	}

}
