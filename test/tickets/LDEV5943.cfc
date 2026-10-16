component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function beforeAll() {
		variables.admin = new org.lucee.cfml.Administrator( "server", request.ServerAdminPassword );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5943 - inspectTemplate setting should persist", function() {

			it( "should read inspectTemplate from config not use hardcoded default", function() {
				// Get the current setting
				var settings = admin.getPerformanceSettings();
				var originalValue = settings.inspectTemplate;

				// Change it to something different
				var newValue = ( originalValue == "once" ) ? "never" : "once";
				admin.updatePerformanceSettings( inspectTemplate=newValue );

				// Verify the change took effect
				var updatedSettings = admin.getPerformanceSettings();
				expect( updatedSettings.inspectTemplate ).toBe( newValue );

				// Restore original value
				admin.updatePerformanceSettings( inspectTemplate=originalValue );

				// Verify restoration
				var restoredSettings = admin.getPerformanceSettings();
				expect( restoredSettings.inspectTemplate ).toBe( originalValue );
			});

			it( "should accept all valid inspectTemplate values", function() {
				var settings = admin.getPerformanceSettings();
				var originalValue = settings.inspectTemplate;

				var validValues = [ "auto", "never", "once", "always" ];

				for ( var value in validValues ) {
					admin.updatePerformanceSettings( inspectTemplate=value );
					var updatedSettings = admin.getPerformanceSettings();
					expect( updatedSettings.inspectTemplate ).toBe( value, "Failed for value: #value#" );
				}

				// Restore original value
				admin.updatePerformanceSettings( inspectTemplate=originalValue );
			});

		});
	}

}
