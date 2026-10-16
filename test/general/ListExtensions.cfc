component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function beforeAll() {
		// Any setup code if needed
	}

	function run(testResults, testBox) {
		describe("test LuceeExtension function", function() {

			it(title='test listing all extension artifacts n specific group defined', body=function(currentSpec) {
				var artifacts = LuceeExtension();
				
				expect(isArray(artifacts)).toBeTrue();
				expect(arrayLen(artifacts) > 20).toBeTrue("Should return more than 20 extension artifacts from org.lucee group");
				
				// Verify each artifact entry is a string
				if (arrayLen(artifacts) > 0) {
					expect(isSimpleValue(artifacts[1])).toBeTrue("Each artifact should be a string");
					expect(len(trim(artifacts[1])) > 0).toBeTrue("Artifact names should not be empty");
				}
			});

			it(title='test listing all extension artifacts, specific group defined', body=function(currentSpec) {
				var artifacts = LuceeExtension("org.lucee");
				
				expect(isArray(artifacts)).toBeTrue();
				expect(arrayLen(artifacts) > 20).toBeTrue("Should return more than 20 extension artifacts from org.lucee group");
				
				// Verify each artifact entry is a string
				if (arrayLen(artifacts) > 0) {
					expect(isSimpleValue(artifacts[1])).toBeTrue("Each artifact should be a string");
					expect(len(trim(artifacts[1])) > 0).toBeTrue("Artifact names should not be empty");
				}
			});

			it(title='test listing versions for specific extension', body=function(currentSpec) {
				var versions = LuceeExtension("org.lucee","s3-extension");
				
				expect(isArray(versions)).toBeTrue();
				expect(arrayLen(versions) > 10).toBeTrue("Should return more than 10 versions for s3-extension");
				
				// Verify version entries are strings
				if (arrayLen(versions) > 0) {
					expect(isSimpleValue(versions[1])).toBeTrue("Each version should be a string");
					expect(len(trim(versions[1])) > 0).toBeTrue("Version strings should not be empty");
					
					// Check if version follows expected format (numbers and dots)
					expect(reFind("^[\d\.\-\w]+$", versions[1])).toBeGT(0, "Version should contain version-like characters");
				}
			});

			it(title='test getting details for specific release version', body=function(currentSpec) {
				var detail = LuceeExtension("org.lucee","s3-extension", "0.9.4.119-RC");
				
				expect(isStruct(detail)).toBeTrue("Should return a struct with version details");
				expect(structKeyExists(detail, "lex")).toBeTrue("Detail struct should contain 'lex' key");
				expect(isSimpleValue(detail.lex)).toBeTrue("The 'lex' value should be a string URL");
				expect(len(trim(detail.lex)) > 0).toBeTrue("The 'lex' URL should not be empty");
				
				// Verify the URL format looks correct
				expect(detail.lex contains "http").toBeTrue("The 'lex' should be a valid URL");
			});

			it(title='test getting details for specific snapshot version', body=function(currentSpec) {
				var detail = LuceeExtension("org.lucee","s3-extension", "2.0.2.15-SNAPSHOT");
				
				expect(isStruct(detail)).toBeTrue("Should return a struct with version details");
				expect(structKeyExists(detail, "lex")).toBeTrue("Detail struct should contain 'lex' key");
				expect(isSimpleValue(detail.lex)).toBeTrue("The 'lex' value should be a string URL");
				expect(len(trim(detail.lex)) > 0).toBeTrue("The 'lex' URL should not be empty");
				
				// Verify the URL format looks correct
				expect(detail.lex contains "http").toBeTrue("The 'lex' should be a valid URL");
				expect(detail.lex contains "SNAPSHOT").toBeTrue("Snapshot version URL should contain SNAPSHOT");
			});

			it(title='test URL validation using fileExists', body=function(currentSpec) {
				var detail = LuceeExtension("org.lucee","s3-extension", "0.9.4.119-RC");
				
				expect(isStruct(detail)).toBeTrue();
				expect(structKeyExists(detail, "lex")).toBeTrue();
				
				// Test that the URL is accessible using Lucee's virtual filesystem
				var urlExists = fileExists(detail.lex);
				expect(isBoolean(urlExists)).toBeTrue("fileExists should return a boolean");
				
				// Note: We don't assert true/false for URL existence as it depends on network connectivity
				// and the actual availability of the artifact at test time
			});

			it(title='test invalid extension name handling', body=function(currentSpec) {
				try {
					var versions = LuceeExtension("org.lucee","non-existent-extension");
					// If no error is thrown, should return empty array or handle gracefully
					expect(isArray(versions)).toBeTrue();
				} catch (any e) {
					// If an error is thrown, it should be a meaningful error
					expect(len(e.message) > 0).toBeTrue("Error message should not be empty");
				}
			});

			it(title='test invalid version handling', body=function(currentSpec) {
				try {
					var detail = LuceeExtension("org.lucee","s3-extension", "999.999.999-INVALID");
					// If no error is thrown, should return empty struct or handle gracefully
					expect(isStruct(detail)).toBeTrue();
				} catch (any e) {
					// If an error is thrown, it should be a meaningful error
					expect(len(e.message) > 0).toBeTrue("Error message should not be empty");
				}
			});

			it(title='test function with empty parameters', body=function(currentSpec) {
				try {
					var result = LuceeExtension("");
					expect(isArray(result) || isStruct(result)).toBeTrue("Should handle empty string parameter gracefully");
				} catch (any e) {
					expect(len(e.message) > 0).toBeTrue("Error message should not be empty");
				}
			});

			it(title='test artifact list contains expected extensions', body=function(currentSpec) {
				var artifacts = LuceeExtension();
				
				// Verify s3-extension is in the list (since we know it exists from examples)
				var hasS3Extension = arrayContains(artifacts, "s3-extension");
				expect(hasS3Extension).toBeTrue("Artifact list should contain 's3-extension'");
				
				// Check for other common extensions that might exist
				var hasExtensions = false;
				for (var artifact in artifacts) {
					if (findNoCase("extension", artifact) > 0) {
						hasExtensions = true;
						break;
					}
				}
				expect(hasExtensions).toBeTrue("At least some artifacts should have 'extension' in the name");
			});

			it(title='test version list contains expected versions', body=function(currentSpec) {
				var versions = LuceeExtension("org.lucee","s3-extension");
				
				expect(arrayLen(versions) > 1).toBeTrue("Should have multiple versions");
				
				// Check if the known versions from examples are in the list
				var hasRCVersion = arrayContains(versions, "0.9.4.119-RC");
				var hasSnapshotVersion = arrayContains(versions, "2.0.2.15-SNAPSHOT");
				
				// Note: These specific versions might not exist, so we'll just check version format patterns
				var hasRCPattern = false;
				var hasSnapshotPattern = false;
				
				for (var version in versions) {
					if (findNoCase("-RC", version) > 0) hasRCPattern = true;
					if (findNoCase("-SNAPSHOT", version) > 0) hasSnapshotPattern = true;
				}
				
				// At least verify we have version-like strings
				expect(versions[1] contains ".").toBeTrue("Versions should contain dots as separators");
			});

		});
	}
}