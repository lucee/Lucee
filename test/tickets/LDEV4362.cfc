component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin,mapping" {

	function beforeAll() {
		variables.pw = request.SERVERADMINPASSWORD ?: server.SERVERADMINPASSWORD;
		variables.cfConfigFile = expandPath( "{lucee-config}/.CFConfig.json" );
		variables.physical = getTempDirectory() & "ldev4362/";
		if ( !directoryExists( variables.physical ) ) directoryCreate( variables.physical );
		variables.virtuals = [];
	}

	function afterAll() {
		loop array=variables.virtuals item="local.virtual" {
			try {
				admin action="removeMapping" type="server" password=variables.pw virtual=virtual;
			}
			catch ( any e ) {}
		}
		if ( directoryExists( variables.physical ) ) directoryDelete( variables.physical, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-4362 mapping attribute webAccessible, topLevel is kept as an alias", function() {

			it( title="cfadmin updateMapping accepts webAccessible", body=function( currentSpec ) {
				var virtual = newVirtual();
				admin action="updateMapping" type="server" password=variables.pw
					virtual=virtual physical=variables.physical archive="" primary="physical" webAccessible=false;

				var m = getMapping( virtual );
				expect( m.webAccessible ).toBeFalse();
				// the old column is still returned
				expect( m.toplevel ).toBeFalse();

				admin action="updateMapping" type="server" password=variables.pw
					virtual=virtual physical=variables.physical archive="" primary="physical" webAccessible=true;
				var m = getMapping( virtual );
				expect( m.webAccessible ).toBeTrue();
				expect( m.toplevel ).toBeTrue();
			});

			it( title="cfadmin updateMapping still accepts the alias toplevel", body=function( currentSpec ) {
				var virtual = newVirtual();
				admin action="updateMapping" type="server" password=variables.pw
					virtual=virtual physical=variables.physical archive="" primary="physical" toplevel=false;

				var m = getMapping( virtual );
				expect( m.webAccessible ).toBeFalse();
				expect( m.toplevel ).toBeFalse();

				admin action="updateMapping" type="server" password=variables.pw
					virtual=virtual physical=variables.physical archive="" primary="physical" toplevel=true;
				var m = getMapping( virtual );
				expect( m.webAccessible ).toBeTrue();
				expect( m.toplevel ).toBeTrue();
			});

			it( title="webAccessible wins when both attributes are passed", body=function( currentSpec ) {
				var virtual = newVirtual();
				admin action="updateMapping" type="server" password=variables.pw
					virtual=virtual physical=variables.physical archive="" primary="physical" webAccessible=false toplevel=true;

				expect( getMapping( virtual ).webAccessible ).toBeFalse();
			});

			it( title="a mapping is web accessible by default", body=function( currentSpec ) {
				var virtual = newVirtual();
				admin action="updateMapping" type="server" password=variables.pw
					virtual=virtual physical=variables.physical archive="" primary="physical";

				var m = getMapping( virtual );
				expect( m.webAccessible ).toBeTrue();
				expect( m.toplevel ).toBeTrue();
			});

			it( title="cfadmin getMapping returns both keys", body=function( currentSpec ) {
				var virtual = newVirtual();
				admin action="updateMapping" type="server" password=variables.pw
					virtual=virtual physical=variables.physical archive="" primary="physical" webAccessible=false;

				admin action="getMapping" type="server" password=variables.pw virtual=virtual returnVariable="local.sct";
				expect( sct ).toHaveKey( "webAccessible" );
				expect( sct ).toHaveKey( "toplevel" );
				expect( sct.webAccessible ).toBeFalse();
				expect( sct.toplevel ).toBeFalse();
			});

			it( title="writes webAccessible to the config and no longer the old name", body=function( currentSpec ) {
				var virtual = newVirtual();
				// first as the alias
				admin action="updateMapping" type="server" password=variables.pw
					virtual=virtual physical=variables.physical archive="" primary="physical" toplevel=false;
				var cfg = persistedMapping( virtual );
				expect( cfg ).toHaveKey( "webAccessible", serializeJSON( cfg ) );
				expect( cfg ).notToHaveKey( "topLevel", serializeJSON( cfg ) );
				expect( cfg.webAccessible ).toBeFalse( serializeJSON( cfg ) );

				admin action="updateMapping" type="server" password=variables.pw
					virtual=virtual physical=variables.physical archive="" primary="physical" webAccessible=true;
				var cfg = persistedMapping( virtual );
				expect( cfg ).notToHaveKey( "topLevel", serializeJSON( cfg ) );
				expect( cfg.webAccessible ).toBeTrue( serializeJSON( cfg ) );
			});

			it( title="a config using the old name topLevel is still read", body=function( currentSpec ) {
				var virtual = newVirtual();
				var data = { "mappings": { "#virtual#": { "physical": variables.physical, "primary": "physical", "topLevel": false } } };
				configImport( data=data, type="server", password=variables.pw );
				expect( getMapping( virtual ).webAccessible ).toBeFalse();

				var virtual2 = newVirtual();
				var data = { "mappings": { "#virtual2#": { "physical": variables.physical, "primary": "physical", "toplevel": false } } };
				configImport( data=data, type="server", password=variables.pw );
				expect( getMapping( virtual2 ).webAccessible ).toBeFalse();
			});

			it( title="a config using webAccessible is read", body=function( currentSpec ) {
				var virtual = newVirtual();
				var data = { "mappings": { "#virtual#": { "physical": variables.physical, "primary": "physical", "webAccessible": false } } };
				configImport( data=data, type="server", password=variables.pw );
				var m = getMapping( virtual );
				expect( m.webAccessible ).toBeFalse();
				expect( m.toplevel ).toBeFalse();
			});

			it( title="webAccessible takes precedence over topLevel in a config", body=function( currentSpec ) {
				var virtual = newVirtual();
				var data = { "mappings": { "#virtual#": { "physical": variables.physical, "primary": "physical", "webAccessible": false, "topLevel": true } } };
				configImport( data=data, type="server", password=variables.pw );
				expect( getMapping( virtual ).webAccessible ).toBeFalse();
			});

		});
	}

	private string function newVirtual() {
		var virtual = "/ldev4362_" & lCase( replace( createUUID(), "-", "", "all" ) );
		arrayAppend( variables.virtuals, virtual );
		return virtual;
	}

	private struct function getMapping( required string virtual ) {
		admin action="getMappings" type="server" password=variables.pw returnVariable="local.mappings";
		loop query=mappings {
			if ( mappings.virtual == arguments.virtual || mappings.virtual == arguments.virtual & "/" ) {
				return {
					"webAccessible": mappings.webAccessible,
					"toplevel": mappings.toplevel
				};
			}
		}
		throw "mapping [#arguments.virtual#] not found";
	}

	private struct function persistedMapping( required string virtual ) {
		var cfg = deserializeJSON( fileRead( variables.cfConfigFile ) );
		var mappings = cfg.mappings ?: {};
		loop collection=mappings key="local.key" value="local.value" {
			if ( key == arguments.virtual || key == arguments.virtual & "/" ) return value;
		}
		throw "mapping [#arguments.virtual#] not found in .CFConfig.json";
	}

}
