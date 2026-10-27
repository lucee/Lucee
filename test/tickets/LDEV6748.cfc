component extends="org.lucee.cfml.test.LuceeTestCase" labels="null" {

	function beforeAll() {
		variables.cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
		variables.system = createObject( "java", "java.lang.System" );
		variables.mac = createObject( "java", "lucee.runtime.listener.ModernApplicationContext" );
		variables.nsh = createObject( "java", "lucee.runtime.config.NullSupportHelper" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6748 server level nullSupport without an application setting", function() {

			it( "argument default null is honoured in a component without nullSupport in the Application.cfc", function() {
				var result = withServerNullSupport( true, function() {
					return _InternalRequest( template: createURI( "LDEV6748/index.cfm" ) ).filecontent.trim();
				});
				expect( result ).toBe( "exists:true;null:true" );
			});

			it( "NullSupportHelper.full() follows the server setting when no application overrides it", function() {
				expect( withServerNullSupport( true, function() {
					return variables.nsh.full();
				}) ).toBeTrue();

				expect( withServerNullSupport( false, function() {
					return variables.nsh.full();
				}) ).toBeFalse();
			});

		});
	}

	// enables/disables null support on server level, as if no application ever defined its own null support setting
	// the test server is started with -Dlucee.full.null.support=false, which takes precedence over .CFConfig.json,
	// so the server setting is switched via that system property
	private any function withServerNullSupport( required boolean nullSupport, required function body ) {
		var hasCustom = variables.mac.hasCustomNullSupportSetting;
		var orgProp = variables.system.getProperty( "lucee.full.null.support" );
		try {
			variables.system.setProperty( "lucee.full.null.support", arguments.nullSupport ? "true" : "false" );
			variables.cs.resetFullNullSupport();
			variables.mac.hasCustomNullSupportSetting = false;
			return arguments.body();
		}
		finally {
			variables.mac.hasCustomNullSupportSetting = hasCustom;
			if ( isNull( orgProp ) ) variables.system.clearProperty( "lucee.full.null.support" );
			else variables.system.setProperty( "lucee.full.null.support", orgProp );
			variables.cs.resetFullNullSupport();
		}
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}
}
