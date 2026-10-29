component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.date = createDateTime( 2021, 1, 29, 11, 45, 26, 123 );
		variables.hadSerialization = getRoot().keyExists( "serialization" );
		if ( variables.hadSerialization ) variables.origSerialization = duplicate( getRoot().serialization );
	}

	function afterAll() {
		setConfigDateFormat( nullValue() );
		if ( variables.hadSerialization ) getRoot().serialization = variables.origSerialization;
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3251 serializeJSON date format (this.serialization.dateFormat and serialization.dateFormat in the config)", function() {

			afterEach( function() {
				setConfigDateFormat( nullValue() );
			} );

			it( "keeps the traditional format by default", function() {
				var result = callApp( "none" );
				expect( result ).toBe( '{"d":"January, 29 2021 11:45:26 +0100"}' );
			} );

			it( "Application.cfc dateFormat=cf keeps the traditional format", function() {
				expect( callApp( "cf" ) ).toBe( '{"d":"January, 29 2021 11:45:26 +0100"}' );
			} );

			it( "Application.cfc dateFormat=iso returns ISO 8601", function() {
				expect( callApp( "iso" ) ).toBe( '{"d":"2021-01-29T11:45:26+01:00"}' );
			} );

			it( "Application.cfc dateFormat=isoms returns ISO 8601 with milliseconds", function() {
				expect( callApp( "isoms" ) ).toBe( '{"d":"2021-01-29T11:45:26.123+01:00"}' );
			} );

			it( "Application.cfc dateFormat accepts a custom mask", function() {
				expect( callApp( "mask" ) ).toBe( '{"d":"2021/01/29 11:45"}' );
			} );

			it( "the Application.cfc key is case insensitive", function() {
				expect( callApp( "lowercase" ) ).toBe( '{"d":"2021-01-29T11:45:26+01:00"}' );
			} );

			it( "throws for an invalid mask and names the setting", function() {
				var result = callApp( "invalid" );
				expect( result ).toInclude( "dateFormat" );
				expect( result ).toInclude( "invalid" );
			} );

			it( "getApplicationSettings exposes the setting", function() {
				expect( deserializeJSON( callApp( "iso", { settings: true } ) ).dateFormat ).toBe( "iso" );
			} );

			it( "the config setting serialization.dateFormat is used when the application does not define one", function() {
				setConfigDateFormat( "iso" );
				expect( callApp( "none" ) ).toBe( '{"d":"2021-01-29T11:45:26+01:00"}' );
				expect( serializeJSON( { "d": variables.date } ) ).toMatch( '^\{"d":"\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d(Z|[+-]\d\d:\d\d)"\}$' );
			} );

			it( "the config setting accepts cf", function() {
				setConfigDateFormat( "cf" );
				expect( callApp( "none" ) ).toBe( '{"d":"January, 29 2021 11:45:26 +0100"}' );
			} );

			it( "the Application.cfc setting overrides the config setting", function() {
				setConfigDateFormat( "iso" );
				expect( callApp( "mask" ) ).toBe( '{"d":"2021/01/29 11:45"}' );
				expect( callApp( "cf" ) ).toBe( '{"d":"January, 29 2021 11:45:26 +0100"}' );
			} );

			it( "an invalid config value throws", function() {
				setConfigDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS_nope[" );
				expect( function() {
					serializeJSON( { "d": variables.date } );
				} ).toThrow( "application" );
			} );

		} );
	}

	private string function callApp( required string mode, struct urlArgs={} ) {
		var args = { mode: arguments.mode };
		structAppend( args, arguments.urlArgs );
		return trim( _InternalRequest( template: createURI( "LDEV3251/index.cfm" ), url: args ).filecontent );
	}

	private function getConfig() {
		return createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
	}

	// the raw config data (root struct) of the server config
	private function getRoot() {
		return getConfig().raw();
	}

	// changes the in memory config only, nothing is written to the .CFConfig.json
	private function setConfigDateFormat( value ) {
		var root = getRoot();
		if ( isNull( arguments.value ) ) {
			if ( variables.hadSerialization ) root.serialization = duplicate( variables.origSerialization );
			else structDelete( root, "serialization" );
		}
		else {
			if ( !root.keyExists( "serialization" ) || !isStruct( root.serialization ) ) root.serialization = {};
			root.serialization.dateFormat = arguments.value;
		}
		getConfig().resetSerializationDateFormat();
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
