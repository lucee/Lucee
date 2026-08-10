component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "Testcase for LDEV-6450 - Password cache issue (general problem with all admin actions that call store())", function(){

			it( "Multiple updateMapping calls should work after setPassword", function() {
				var testPassword = "testpass_6450_" & randRange(10000,99999);
				var testVirtual1 = "/test_6450_mapping1_" & randRange(10000,99999);
				var testVirtual2 = "/test_6450_mapping2_" & randRange(10000,99999);
				var testPhysical = getTempDirectory() & "lucee_test_6450/";

				// Ensure test directory exists
				if (!directoryExists(testPhysical)) {
					directoryCreate(testPhysical);
				}

				try {
					// Step 0: Set server admin password (may already be set, that's ok)
					try {
						admin
							action="updatePassword"
							type="server"
							oldPassword="admin"
							newPassword=testPassword;
					}
					catch(e){
						// Password may already be set from previous test run, ignore
					}

					// Step 1: Create first mapping with the password
					admin
						action="updateMapping"
						type="server"
						password=testPassword
						virtual=testVirtual1
						physical=testPhysical
						toplevel="true"
						primary="physical";

					// Step 2: Create second mapping with the same password
					// This should NOT fail with "No access, password is invalid"
					// Bug: after first updateMapping, password cache is stale
					admin
						action="updateMapping"
						type="server"
						password=testPassword
						virtual=testVirtual2
						physical=testPhysical
						toplevel="true"
						primary="physical";

					// Step 3: Verify both mappings exist
					var mappings = [];
					admin
						action="getMappings"
						type="server"
						password=testPassword
						returnVariable="mappings";

					var mapping1Found = false;
					var mapping2Found = false;

					for (var mapping in mappings) {
						if (mapping.virtual == testVirtual1) mapping1Found = true;
						if (mapping.virtual == testVirtual2) mapping2Found = true;
					}

					expect(mapping1Found).toBe(true, "First mapping should exist");
					expect(mapping2Found).toBe(true, "Second mapping should exist after second updateMapping");

				}
				catch (any e) {
					fail("Should be able to create multiple mappings after setting password. Error: " & e.message);
				}
				finally {
					// Cleanup: Remove test mappings
					try {
						admin
							action="removeMapping"
							type="server"
							password=testPassword
							virtual=testVirtual1;
					}
					catch (any e) {
						// Ignore cleanup errors
					}

					try {
						admin
							action="removeMapping"
							type="server"
							password=testPassword
							virtual=testVirtual2;
					}
					catch (any e) {
						// Ignore cleanup errors
					}

					// Remove test directory
					try {
						if (directoryExists(testPhysical)) {
							directoryDelete(testPhysical, true);
						}
					}
					catch (any e) {
						// Ignore cleanup errors
					}
				}
			});

			it( "Mixed admin actions (updateMapping + removeMapping) should work after setPassword - proves password cache affects all admin actions", function() {
				var testPassword = "testpass_6450_" & randRange(10000,99999);
				var testVirtual = "/test_6450_mixed_" & randRange(10000,99999);
				var testPhysical = getTempDirectory() & "lucee_test_6450_mixed/";

				if (!directoryExists(testPhysical)) {
					directoryCreate(testPhysical);
				}

				try {
					// Step 0: Set server admin password (may already be set, that's ok)
					try {
						admin
							action="updatePassword"
							type="server"
							oldPassword="admin"
							newPassword=testPassword;
					}
					catch(e){
						// Password may already be set from previous test run, ignore
					}

					// Step 1: Create mapping with the password
					admin
						action="updateMapping"
						type="server"
						password=testPassword
						virtual=testVirtual
						physical=testPhysical
						toplevel="true"
						primary="physical";

					// Step 2: Remove mapping with the same password
					// This should NOT fail - proving password cache issue affects ALL admin actions
					admin
						action="removeMapping"
						type="server"
						password=testPassword
						virtual=testVirtual;

					// Step 3: Verify mapping is removed
					var mappings = [];
					admin
						action="getMappings"
						type="server"
						password=testPassword
						returnVariable="mappings";

					var mappingFound = false;
					for (var mapping in mappings) {
						if (mapping.virtual == testVirtual) mappingFound = true;
					}

					expect(mappingFound).toBe(false, "Mapping should be removed after removeMapping");

				}
				catch (any e) {
					fail("Should be able to remove mapping after setting password. Error: " & e.message & " - This confirms password cache issue is GENERAL to all admin actions");
				}
				finally {
					try {
						if (directoryExists(testPhysical)) {
							directoryDelete(testPhysical, true);
						}
					}
					catch (any e) {
						// Ignore cleanup errors
					}
				}
			});

			it( "Multiple updateRegional calls should work after setPassword - proves password cache issue affects ALL admin actions", function() {
				var testPassword = "testpass_6450_" & randRange(10000,99999);

				try {
					// Step 0: Set server admin password (may already be set, that's ok)
					try {
						admin
							action="updatePassword"
							type="server"
							oldPassword="admin"
							newPassword=testPassword;
					}
					catch(e){
						// Password may already be set from previous test run, ignore
					}

					// Step 1: Update regional setting (US)
					admin
						action="updateRegional"
						type="server"
						password=testPassword
						locale="en_US"
						timeZone="America/New_York";

					// Step 2: Update regional setting (UK) with same password
					// This should NOT fail - proving password cache issue is GENERAL
					admin
						action="updateRegional"
						type="server"
						password=testPassword
						locale="en_GB"
						timeZone="Europe/London";

					// If we get here, both calls succeeded
					expect(true).toBe(true, "Both updateRegional calls should succeed");

				}
				catch (any e) {
					fail("Should be able to update multiple regional settings after setting password. Error: " & e.message & " - This confirms password cache issue is GENERAL to ALL admin actions");
				}
			});
		});
	}
}
