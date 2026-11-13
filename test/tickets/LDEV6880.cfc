component extends="org.lucee.cfml.test.LuceeTestCase" labels="cfx" {

	function run( testResults, testBox ) {
		describe( title="LDEV-6880 adding a CFX tag must not break the CFX tag pool", body=function() {

			it( title="updateJavaCFX with an empty class throws and does not write an entry", body=function( currentSpec ) {
				var name = createName();
				var good = createName();
				try {
					expect( function() {
						admin action="updateJavaCFX" type="server" password="#request.ServerAdminPassword#"
							name="#name#" class="";
					}).toThrow();

					// a successful update stores the config and resets the cfx tag pool
					admin action="updateJavaCFX" type="server" password="#request.ServerAdminPassword#"
						name="#good#" class="lucee.cfx.example.HelloWorld";

					expect( getStoredCFX() ).notToHaveKey( name );
					var javaNames = getJavaCFXTagNames();
					expect( javaNames ).toInclude( good );
					expect( javaNames ).notToInclude( name );
				}
				finally {
					removeCFX( [ name, good ] );
				}
			});

			it( title="updateJavaCFX with a class that is not a CFX tag throws and does not write an entry", body=function( currentSpec ) {
				var name = createName();
				var good = createName();
				try {
					expect( function() {
						admin action="updateJavaCFX" type="server" password="#request.ServerAdminPassword#"
							name="#name#" class="lucee.runtime.type.StructImpl";
					}).toThrow( regex="com.allaire.cfx.CustomTag" );

					admin action="updateJavaCFX" type="server" password="#request.ServerAdminPassword#"
						name="#good#" class="lucee.cfx.example.HelloWorld";

					expect( getStoredCFX() ).notToHaveKey( name );
					var javaNames = getJavaCFXTagNames();
					expect( javaNames ).toInclude( good );
					expect( javaNames ).notToInclude( name );
				}
				finally {
					removeCFX( [ name, good ] );
				}
			});

			it( title="invalid cfx entries in the config are skipped and the CFX tag list still loads", body=function( currentSpec ) {
				var cs = getPageContext().getConfig().getConfigServer( request.ServerAdminPassword );
				var cfx = cs.raw().cfx;
				var invalid = [:];
				invalid[ createName() ] = {}; // no type
				invalid[ createName() ] = { "type": "java" }; // no class
				invalid[ createName() ] = { "type": "cpp", "class": "lucee.cfx.example.HelloWorld" }; // not supported type
				try {
					structAppend( cfx, invalid );
					cs.resetCFXTagPool();

					admin action="getCFXTags" type="server" password="#request.ServerAdminPassword#" returnVariable="local.tags";
					var names = valueList( tags.name );
					var javaNames = getJavaCFXTagNames();
					loop collection=invalid key="local.key" {
						expect( names ).notToInclude( key );
						expect( javaNames ).notToInclude( key );
					}
				}
				finally {
					loop collection=invalid key="local.key" {
						structDelete( cfx, key );
					}
					cs.resetCFXTagPool();
				}
			});

		});
	}

	private string function createName() {
		return "ldev6880_" & replace( lCase( createUUID() ), "-", "", "all" );
	}

	private string function getJavaCFXTagNames() {
		admin action="getJavaCFXTags" type="server" password="#request.ServerAdminPassword#" returnVariable="local.tags";
		return valueList( tags.name );
	}

	private struct function getStoredCFX() {
		var cfg = deserializeJSON( fileRead( expandPath( "{lucee-config}/.CFConfig.json" ) ) );
		return cfg.cfx ?: {};
	}

	private void function removeCFX( required array names ) {
		loop array=arguments.names item="local.name" {
			try {
				admin action="removeCFX" type="server" password="#request.ServerAdminPassword#" name="#name#";
			}
			catch ( any e ) {}
		}
	}

}
