component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function beforeAll() {
		variables.admin = new org.lucee.cfml.Administrator( "server", request.ServerAdminPassword );
		variables.originalValue = admin.getPerformanceSettings().inspectTemplate;
	}

	function afterAll() {
		admin.updatePerformanceSettings( inspectTemplate=variables.originalValue );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-4931 getApplicationSettings() returns inspectTemplate", function() {

			it( "includes inspectTemplate and matches the admin setting", function() {
				var as = getApplicationSettings();
				expect( as ).toHaveKey( "inspectTemplate" );
				expect( as.inspectTemplate ).toBe( admin.getPerformanceSettings().inspectTemplate );
			});

			it( "reports every inspectTemplate value, including auto", function() {
				loop array=[ "auto", "never", "always", "once" ] item="local.value" {
					admin.updatePerformanceSettings( inspectTemplate=value );
					expect( getApplicationSettings().inspectTemplate ).toBe( value, "inspectTemplate [#value#]" );
				}
			});

		});
	}

}
