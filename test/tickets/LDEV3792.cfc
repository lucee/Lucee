component extends="org.lucee.cfml.test.LuceeTestCase" labels="monitor" javaSettings='{ "loadPaths": [ "./LDEV3792/lib/BlockingRequestMonitor.jar" ] }' {

	function beforeAll() {
		variables.uri = createURI( "LDEV3792/index.cfm" );
		variables.adminPassword = request.ServerAdminPassword;
		admin action="isMonitorEnabled" type="server" password=variables.adminPassword returnVariable="variables.wasEnabled";
		admin action="updateMonitor" type="server" password=variables.adminPassword
			class="lucee.test.ldev3792.BlockingRequestMonitor" monitorType="request" name="LDEV3792" logEnabled=true;
		// the monitors are loaded lazily, load them now so the class gets loaded with the javaSettings of this test
		var configServer = getConfigServer();
		configServer.resetMonitors();
		configServer.getRequestMonitors();
	}

	function afterAll() {
		setMonitoringEnabled( variables.wasEnabled );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3792 a request monitor can abort the request from init(PageContext)", function() {

			it( title="a request the monitor lets through is executed", body=function( currentSpec ) {
				setMonitoringEnabled( true );
				var result = _InternalRequest( template: variables.uri );
				expect( result.status ).toBe( 200 );
				expect( trim( result.filecontent ) ).toBe( "onRequestStart executed;page executed" );
			} );

			it( title="a request the monitor aborts returns the response of the monitor and never reaches the Application.cfc", body=function( currentSpec ) {
				setMonitoringEnabled( true );
				var result = _InternalRequest( template: variables.uri, urls: { block: true } );
				expect( result.status ).toBe( 403 );
				expect( trim( result.filecontent ) ).toBe( "blocked by request monitor" );
			} );

			it( title="the monitor is not called when monitoring is disabled", body=function( currentSpec ) {
				setMonitoringEnabled( false );
				var result = _InternalRequest( template: variables.uri, urls: { block: true } );
				expect( result.status ).toBe( 200 );
				expect( trim( result.filecontent ) ).toBe( "onRequestStart executed;page executed" );
			} );

		} );
	}

	private function getConfigServer() {
		return createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
	}

	private void function setMonitoringEnabled( required boolean enabled ) {
		admin action="updateMonitorEnabled" type="server" password=variables.adminPassword monitorEnabled=arguments.enabled;
		getConfigServer().resetMonitoringEnabled();
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
