component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function run( testResults, testBox ) {
		describe( "LDEV-6787 constants defined in the configuration", function() {

			it( title="loads one constant", body=function( currentSpec ) {
				var loaded = load( { "ldev6787-a": "/ldev6787/a" } );
				expect( loaded.len() ).toBe( 1 );
				expect( loaded[ "ldev6787-a" ] ).toBe( "/ldev6787/a" );
			});

			it( title="loads two constants", body=function( currentSpec ) {
				var loaded = load( { "ldev6787-a": "/ldev6787/a", "ldev6787-b": "/ldev6787/b" } );
				expect( loaded.len() ).toBe( 2 );
				expect( loaded[ "ldev6787-a" ] ).toBe( "/ldev6787/a" );
				expect( loaded[ "ldev6787-b" ] ).toBe( "/ldev6787/b" );
			});

			it( title="loads three constants", body=function( currentSpec ) {
				var loaded = load( { "ldev6787-a": "/ldev6787/a", "ldev6787-b": "/ldev6787/b", "ldev6787-c": "/ldev6787/c" } );
				expect( loaded.len() ).toBe( 3 );
				expect( loaded[ "ldev6787-a" ] ).toBe( "/ldev6787/a" );
				expect( loaded[ "ldev6787-b" ] ).toBe( "/ldev6787/b" );
				expect( loaded[ "ldev6787-c" ] ).toBe( "/ldev6787/c" );
			});

			it( title="skips a constant without a simple value and keeps the others", body=function( currentSpec ) {
				var loaded = load( { "ldev6787-a": "/ldev6787/a", "ldev6787-b": { "x": 1 }, "ldev6787-c": "/ldev6787/c" } );
				expect( loaded.len() ).toBe( 2 );
				expect( loaded[ "ldev6787-a" ] ).toBe( "/ldev6787/a" );
				expect( loaded[ "ldev6787-c" ] ).toBe( "/ldev6787/c" );
			});

			it( title="loaded constants resolve in a path", body=function( currentSpec ) {
				var base = getTempDirectory() & "ldev6787-" & createUUID();
				var constants = getPageContext().getConfig().getConstants();
				var loaded = load( { "ldev6787-lib": base, "ldev6787-other": base & "/other" } );
				try {
					loop collection=loaded item="local.v" index="local.k" {
						constants[ k ] = v;
					}
					expect( expandPath( "{ldev6787-lib}/sub/file.txt" ) ).toBe( base & "/sub/file.txt" );
					expect( expandPath( "{ldev6787-other}/x" ) ).toBe( base & "/other/x" );
				}
				finally {
					loop collection=loaded item="local.v" index="local.k" {
						structDelete( constants, k );
					}
				}
			});
		});
	}

	private struct function load( required struct constants ) {
		var cfg = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
		var root = { "constants": arguments.constants };
		var loaded = createObject( "java", "lucee.runtime.config.ConfigFactoryImpl" ).loadConstants( cfg, root, { "failed": true } );
		expect( loaded ).notToHaveKey( "failed" );
		return loaded;
	}
}
