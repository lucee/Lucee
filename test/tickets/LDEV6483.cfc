component extends="org.lucee.cfml.test.LuceeTestCase" labels="mappings" {

	function beforeAll() {
		variables.cfg = getPageContext().getConfig();
		variables.configUtil = createObject( "java", "lucee.runtime.config.ConfigUtil" );
		variables.luceeMapping = getMapping( "/lucee" );
		variables.luceeServerMapping = getMapping( "/lucee-server" );

		// a template that lives directly in the physical directory of the /lucee-server mapping,
		// i.e. where Lucee looks for {lucee-server}/context/Server.cfc
		variables.templateName = "LDEV6483_#createUniqueID()#.cfc";
		variables.templateFile = variables.luceeServerMapping.getPhysical().getRealResource( variables.templateName );
		fileWrite( variables.templateFile.getAbsolutePath(), "component {}" );
	}

	function afterAll() {
		if ( !isNull( variables.templateFile ) && variables.templateFile.exists() )
			variables.templateFile.delete();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6483 ConfigUtil.getPageSourceExisting() with a server config that only has /lucee and /lucee-server", function() {

			it( title="finds /lucee-server/{template} when /lucee-server is the LAST mapping (no / root mapping)", body=function( currentSpec ) {
				// this is the order seen at startup when .CFConfig.json has no extra mappings
				var ps = resolve( [ variables.luceeMapping, variables.luceeServerMapping ] );
				expect( isNull( ps ) ).toBeFalse( "page source for /lucee-server/#variables.templateName# not found" );
				expect( ps.getResource().getAbsolutePath() ).toBe( variables.templateFile.getAbsolutePath() );
			});

			it( title="finds /lucee-server/{template} when /lucee-server is NOT the last mapping", body=function( currentSpec ) {
				var ps = resolve( [ variables.luceeServerMapping, variables.luceeMapping ] );
				expect( isNull( ps ) ).toBeFalse( "page source for /lucee-server/#variables.templateName# not found" );
				expect( ps.getResource().getAbsolutePath() ).toBe( variables.templateFile.getAbsolutePath() );
			});

			it( title="finds /lucee-server/{template} when /lucee-server is the only mapping", body=function( currentSpec ) {
				var ps = resolve( [ variables.luceeServerMapping ] );
				expect( isNull( ps ) ).toBeFalse( "page source for /lucee-server/#variables.templateName# not found" );
				expect( ps.getResource().getAbsolutePath() ).toBe( variables.templateFile.getAbsolutePath() );
			});

		});
	}

	// same call CFMLEngineImpl.OnStart makes for Server.cfc, but against a config that only returns the given mappings
	private function resolve( required array mappings ) {
		var cfg = toConfigProxy( toMappingArray( arguments.mappings ) );
		return variables.configUtil.getPageSourceExisting(
			nullValue(), cfg, nullValue(), "/lucee-server/" & variables.templateName,
			true,  // onlyTopLevel
			true,  // useSpecialMappings
			true,  // useDefaultMapping
			false  // onlyPhysicalExisting
		);
	}

	// a ConfigPro that only knows the given mappings (java.lang.reflect.Proxy, see LDEV6483/MappingsOnlyConfig.cfc)
	private function toConfigProxy( required any mappings ) {
		var cl = variables.cfg.getClass().getClassLoader();
		var reflectArray = createObject( "java", "java.lang.reflect.Array" );
		var interfaces = reflectArray.newInstance( cl.loadClass( "java.lang.Class" ), 1 );
		reflectArray.set( interfaces, 0, cl.loadClass( "lucee.runtime.config.ConfigPro" ) );
		var handler = createDynamicProxy( new LDEV6483.MappingsOnlyConfig( arguments.mappings ), [ "java.lang.reflect.InvocationHandler" ] );
		return createObject( "java", "java.lang.reflect.Proxy" ).newProxyInstance( cl, interfaces, handler );
	}

	private function getMapping( required string virtual ) {
		var mappings = variables.cfg.getConfigServerImpl().getMappings();
		for ( var m in mappings ) {
			if ( m.getVirtual() == arguments.virtual ) return m;
		}
		throw "mapping [#arguments.virtual#] not found in server config";
	}

	private function toMappingArray( required array mappings ) {
		var reflectArray = createObject( "java", "java.lang.reflect.Array" );
		var arr = reflectArray.newInstance( arguments.mappings[ 1 ].getClass(), arrayLen( arguments.mappings ) );
		for ( var i = 1; i <= arrayLen( arguments.mappings ); i++ ) {
			reflectArray.set( arr, i - 1, arguments.mappings[ i ] );
		}
		return arr;
	}
}
