component extends="org.lucee.cfml.test.LuceeTestCase" labels="monitor" javaSettings='{ "loadPaths": [ "./LDEV3792/lib/BlockingRequestMonitor.jar" ] }' {

	function beforeAll() {
		variables.adminPassword = request.ServerAdminPassword;
		variables.name = "LDEV3792-remove";
	}

	function afterAll() {
		// no matter what a spec did, do not leave the monitor behind in the server config
		admin action="removeMonitor" type="server" password=variables.adminPassword monitorType="request" name=variables.name;
		getConfigServer().resetMonitors();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3792 removeMonitor removes a monitor from the server config", function() {

			it( title="the monitor is gone after removeMonitor", body=function( currentSpec ) {
				admin action="updateMonitor" type="server" password=variables.adminPassword
					class="lucee.test.ldev3792.BlockingRequestMonitor" monitorType="request" name=variables.name logEnabled=true;
				getConfigServer().resetMonitors();
				expect( hasMonitor( variables.name ) ).toBeTrue();

				admin action="removeMonitor" type="server" password=variables.adminPassword monitorType="request" name=variables.name;
				getConfigServer().resetMonitors();
				expect( hasMonitor( variables.name ) ).toBeFalse();
			} );

			it( title="removeMonitor only removes the monitor with the given name", body=function( currentSpec ) {
				var other = variables.name & "-other";
				try {
					admin action="updateMonitor" type="server" password=variables.adminPassword
						class="lucee.test.ldev3792.BlockingRequestMonitor" monitorType="request" name=variables.name logEnabled=true;
					admin action="updateMonitor" type="server" password=variables.adminPassword
						class="lucee.test.ldev3792.BlockingRequestMonitor" monitorType="request" name=other logEnabled=true;
					getConfigServer().resetMonitors();
					expect( hasMonitor( variables.name ) ).toBeTrue();
					expect( hasMonitor( other ) ).toBeTrue();

					admin action="removeMonitor" type="server" password=variables.adminPassword monitorType="request" name=variables.name;
					getConfigServer().resetMonitors();
					expect( hasMonitor( variables.name ) ).toBeFalse();
					expect( hasMonitor( other ) ).toBeTrue();
				}
				finally {
					admin action="removeMonitor" type="server" password=variables.adminPassword monitorType="request" name=other;
					getConfigServer().resetMonitors();
				}
			} );

		} );
	}

	private boolean function hasMonitor( required string name ) {
		admin action="getMonitors" type="server" password=variables.adminPassword returnVariable="local.monitors";
		return listFindNoCase( valueList( local.monitors.name ), arguments.name ) gt 0;
	}

	private function getConfigServer() {
		return createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
	}

}
