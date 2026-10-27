component extends="org.lucee.cfml.test.LuceeTestCase" labels="null" {

	function beforeAll() {
		variables.admin = new org.lucee.cfml.Administrator( "server", request.ServerAdminPassword );
		variables.compilerSettings = admin.getCompilerSettings();
		variables.mac = createObject( "java", "lucee.runtime.listener.ModernApplicationContext" );
		variables.nsh = createObject( "java", "lucee.runtime.config.NullSupportHelper" );
	}

	function afterAll() {
		setServerNullSupport( variables.compilerSettings.nullSupport );
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
	private any function withServerNullSupport( required boolean nullSupport, required function body ) {
		var hasCustom = variables.mac.hasCustomNullSupportSetting;
		try {
			setServerNullSupport( arguments.nullSupport );
			variables.mac.hasCustomNullSupportSetting = false;
			return arguments.body();
		}
		finally {
			variables.mac.hasCustomNullSupportSetting = hasCustom;
			setServerNullSupport( variables.compilerSettings.nullSupport );
		}
	}

	private void function setServerNullSupport( required boolean nullSupport ) {
		var settings = duplicate( variables.compilerSettings );
		settings.nullSupport = arguments.nullSupport;
		variables.admin.updateCompilerSettings( argumentCollection = settings );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}
}
