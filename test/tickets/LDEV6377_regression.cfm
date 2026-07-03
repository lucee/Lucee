<cfscript>
// Direct regression test for LDEV-6377
// Tests that Java boolean getter (is*) methods are resolved correctly

writeln("==== LDEV-6377 Regression Test ====");
writeln("");

// Test 1: File.hidden via is*() method
writeln("Test 1: Accessing java.io.File.hidden (is*() getter)");
try {
	var File = createObject("java", "java.io.File");
	var testFile = File.init("/testfile.txt");
	var result = testFile.hidden;
	if (isBoolean(result)) {
		writeln("  ✓ PASS: File.hidden resolved to isHidden(), returned: " & result);
	} else {
		writeln("  ✗ FAIL: File.hidden did not return a boolean, got: " & typeof(result));
	}
} catch (any e) {
	writeln("  ✗ FAIL: " & e.message);
}

writeln("");

// Test 2: File.absolute via is*() method
writeln("Test 2: Accessing java.io.File.absolute (is*() getter)");
try {
	var File = createObject("java", "java.io.File");
	var testFile = File.init("/test");
	var result = testFile.absolute;
	if (isBoolean(result)) {
		writeln("  ✓ PASS: File.absolute resolved to isAbsolute(), returned: " & result);
	} else {
		writeln("  ✗ FAIL: File.absolute did not return a boolean, got: " & typeof(result));
	}
} catch (any e) {
	writeln("  ✗ FAIL: " & e.message);
}

writeln("");

// Test 3: File.directory via is*() method
writeln("Test 3: Accessing java.io.File.directory (is*() getter)");
try {
	var File = createObject("java", "java.io.File");
	var testFile = File.init("/test");
	var result = testFile.directory;
	if (isBoolean(result)) {
		writeln("  ✓ PASS: File.directory resolved to isDirectory(), returned: " & result);
	} else {
		writeln("  ✗ FAIL: File.directory did not return a boolean, got: " & typeof(result));
	}
} catch (any e) {
	writeln("  ✗ FAIL: " & e.message);
}

writeln("");

// Test 4: File.file via is*() method
writeln("Test 4: Accessing java.io.File.file (is*() getter)");
try {
	var File = createObject("java", "java.io.File");
	var testFile = File.init("/testfile.txt");
	var result = testFile.file;
	if (isBoolean(result)) {
		writeln("  ✓ PASS: File.file resolved to isFile(), returned: " & result);
	} else {
		writeln("  ✗ FAIL: File.file did not return a boolean, got: " & typeof(result));
	}
} catch (any e) {
	writeln("  ✗ FAIL: " & e.message);
}

writeln("");

// Test 5: Backward compatibility - get*() methods still work
writeln("Test 5: Backward compatibility - get*() methods still work");
try {
	var File = createObject("java", "java.io.File");
	var testFile = File.init("/test/path/file.txt");
	var result = testFile.parent;
	if (isString(result)) {
		writeln("  ✓ PASS: File.parent resolved to getParent(), returned: " & result);
	} else {
		writeln("  ✗ FAIL: File.parent did not return a string, got: " & typeof(result));
	}
} catch (any e) {
	writeln("  ✗ FAIL: " & e.message);
}

writeln("");

// Test 6: Missing property throws exception
writeln("Test 6: Missing property throws ApplicationException");
try {
	var File = createObject("java", "java.io.File");
	var testFile = File.init("/testfile.txt");
	var result = testFile.nonexistentProperty;
	writeln("  ✗ FAIL: Should have thrown an exception for missing property");
} catch (any e) {
	if (e.type == "application" && findNoCase("nonexistentProperty", e.message)) {
		writeln("  ✓ PASS: Properly threw ApplicationException: " & e.message);
	} else {
		writeln("  ✗ FAIL: Wrong exception type: " & e.type & " - " & e.message);
	}
}

writeln("");
writeln("==== Test Complete ====");
