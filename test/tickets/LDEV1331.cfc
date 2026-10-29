component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function beforeAll() {
		variables.cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
		variables.sys = createObject( "java", "java.lang.System" );
		variables.prefix = "ldev1331";
	}

	function afterAll() {
		variables.sys.clearProperty( "ldev1331.prop" );
		_restore();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1331 config constants support placeholders", function() {

			it( title="plain values stay as they are", body=function( currentSpec ) {
				var c = _constants( { "ldev1331plain": "/some/path/{web-root}/x", "ldev1331noph": "/other/path" } );
				expect( c.ldev1331plain ).toBe( "/some/path/{web-root}/x" );
				expect( c.ldev1331noph ).toBe( "/other/path" );
			});

			it( title="${NAME:default} falls back to the default", body=function( currentSpec ) {
				var c = _constants( { "ldev1331dollar": "/base/${LDEV1331_UNDEFINED_VAR:fallback}/lib" } );
				expect( c.ldev1331dollar ).toBe( "/base/fallback/lib" );
			});

			it( title="{system:name}", body=function( currentSpec ) {
				variables.sys.setProperty( "ldev1331.prop", "propval" );
				var c = _constants( { "ldev1331sys": "/base/{system:ldev1331.prop}/lib" } );
				expect( c.ldev1331sys ).toBe( "/base/propval/lib" );
			});

			it( title="{env:NAME}", body=function( currentSpec ) {
				var envs = variables.sys.getenv();
				var name = "";
				for ( var k in envs ) {
					if ( len( envs[ k ] ) > 0 && len( envs[ k ] ) < 30 && reFind( "^[A-Za-z_]+$", k ) && !findNoCase( "pass", k ) && !findNoCase( "secret", k ) && !findNoCase( "token", k ) && !findNoCase( "key", k ) ) {
						name = k;
						break;
					}
				}
				if ( len( name ) ) {
					var c = _constants( { "ldev1331env": "/base/{env:#name#}/lib" } );
					expect( c.ldev1331env == "/base/#envs[ name ]#/lib" ).toBeTrue();
				}
			});

			it( title="several placeholders in one value, other constants are not touched", body=function( currentSpec ) {
				variables.sys.setProperty( "ldev1331.prop", "propval" );
				var c = _constants( { "ldev1331multi": "{system:ldev1331.prop}-${LDEV1331_UNDEFINED_VAR:a}", "ldev1331other": "untouched" } );
				expect( c.ldev1331multi ).toBe( "propval-a" );
				expect( c.ldev1331other ).toBe( "untouched" );
			});

			it( title="the resolved constant can be used in a placeholder", body=function( currentSpec ) {
				_constants( { "ldev1331ref": "${LDEV1331_UNDEFINED_VAR:resolved}" } );
				expect( variables.cs.replacePlaceHolder( "${ldev1331ref}" ) ).toBe( "resolved" );
			});
		});
	}

	private struct function _constants( required struct values ) {
		_restore();
		var root = variables.cs.raw();
		if ( !structKeyExists( root, "constants" ) ) root.constants = {};
		loop collection=arguments.values index="local.k" item="local.v" {
			root.constants[ k ] = v;
		}
		variables.cs.resetConstants();
		var result = {};
		var consts = variables.cs.getConstants();
		for ( var k in arguments.values ) result[ k ] = consts[ k ];
		return result;
	}

	private void function _restore() {
		var root = variables.cs.raw();
		if ( structKeyExists( root, "constants" ) && isStruct( root.constants ) ) {
			for ( var k in structKeyList( root.constants ) ) {
				if ( left( k, 7 ) == "ldev1331" ) structDelete( root.constants, k );
			}
		}
		variables.cs.resetConstants();
	}
}
