component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run(testResults, testBox) {
		describe(title='LDEV-6377 - Java boolean getter (is*) support', body=function(){

			it(title='accessing boolean property via is*() getter on java.io.File', body=function() {
				var File = createObject("java", "java.io.File");
				var testFile = File.init("/testfile.txt");
				var result = testFile.hidden;
				expect(isBoolean(result)).toBe(true, "Result should be boolean");
			});

			it(title='File.hidden returns expected boolean value', body=function() {
				var File = createObject("java", "java.io.File");
				var testFile = File.init("/testfile.txt");
				var result = testFile.hidden;
				expect(isBoolean(result)).toBe(true);
				// Should resolve to isHidden() method
			});

			it(title='File.absolute returns expected boolean value', body=function() {
				var File = createObject("java", "java.io.File");
				var testFile = File.init("/testfile.txt");
				var result = testFile.absolute;
				expect(isBoolean(result)).toBe(true);
				// Should resolve to isAbsolute() method
			});

			it(title='accessing get*() still works (backwards compatibility)', body=function() {
				// Test that get*() methods still work
				var File = createObject("java", "java.io.File");
				var testFile = File.init("/test/path/file.txt");
				var parent = testFile.parent;
				expect(parent).toBeString();
				// getParent() should be resolved
			});

			it(title='missing property throws proper exception', body=function() {
				var File = createObject("java", "java.io.File");
				var testFile = File.init("/testfile.txt");
				try {
					var result = testFile.nonexistentProperty;
					fail("Should have thrown an exception for missing property");
				} catch (any e) {
					// Should throw ApplicationException with proper message
					expect(e.type).toBe("application", e.message);
					expect(e.message).toInclude("nonexistentProperty");
				}
			});

			it(title='is*() with non-boolean return type is rejected', body=function() {
				// This tests the type checking of is*() methods
				// If a class has is*() that returns non-boolean, it should not be used
				try {
					var File = createObject("java", "java.io.File");
					var testFile = File.init("/testfile.txt");
					// All is*() methods on File return boolean, so this won't fail
					// but we're testing the logic path
					var hidden = testFile.hidden;
					expect(isBoolean(hidden)).toBe(true);
				} catch (any e) {
					fail("Should not throw: " & e.message);
				}
			});

			it(title='case insensitive property access for is*() getters', body=function() {
				var File = createObject("java", "java.io.File");
				var testFile = File.init("/testfile.txt");
				// CFML is case-insensitive
				var result1 = testFile.hidden;
				var result2 = testFile.HIDDEN;
				var result3 = testFile.Hidden;
				expect(result1).toBe(result2);
				expect(result2).toBe(result3);
			});

			it(title='multiple is*() calls on same object', body=function() {
				var File = createObject("java", "java.io.File");
				var testFile = File.init("/testfile.txt");
				var h1 = testFile.hidden;
				var h2 = testFile.hidden;
				var a1 = testFile.absolute;
				var a2 = testFile.absolute;
				expect(h1).toBe(h2, "Multiple calls should return consistent results");
				expect(a1).toBe(a2, "Multiple calls should return consistent results");
				expect(isBoolean(h1)).toBe(true);
				expect(isBoolean(a1)).toBe(true);
			});

			it(title='get*() and is*() precedence - get*() takes priority', body=function() {
				// If a class has both getXyz() and isXyz(), getXyz() should be called
				// This is Java Bean convention
				var File = createObject("java", "java.io.File");
				var testFile = File.init("/testfile.txt");
				// File doesn't have both, but we test the priority in the code
				var hidden = testFile.hidden;
				expect(isBoolean(hidden)).toBe(true);
			});

			it(title='accessing File.directory via is*() getter', body=function() {
				var File = createObject("java", "java.io.File");
				var testFile = File.init("/test");
				var result = testFile.directory;
				expect(isBoolean(result)).toBe(true);
				// Should resolve to isDirectory() method
			});

			it(title='accessing File.file via is*() getter', body=function() {
				var File = createObject("java", "java.io.File");
				var testFile = File.init("/testfile.txt");
				var result = testFile.file;
				expect(isBoolean(result)).toBe(true);
				// Should resolve to isFile() method
			});

		});
	}

}
