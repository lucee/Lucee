component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "Testcase for LDEV-6450 - Password cache issue after updateMapping", function(){

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
					// Step 1: Set server admin password
					admin
						action="updatePassword"
						type="server"
						oldPassword="admin"
						newPassword=testPassword;

					// Step 2: Create first mapping with the new password
					admin
						action="updateMapping"
						type="server"
						password=testPassword
						virtual=testVirtual1
						physical=testPhysical
						toplevel="true"
						primary="physical";

					// Step 3: Create second mapping with the same password
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

					// Step 4: Verify both mappings exist
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
		});
	}
}
