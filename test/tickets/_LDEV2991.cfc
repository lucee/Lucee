component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testresults , testbox ) {
		describe( "testcase for LDEV-2991", function () {
			it( title = "Checking REReplaceNoCase ,with case conversation", body=function( currentSpec ) {
				myvar = reReplaceNoCase("test server start", "test(\.exe)? ", "C:\/Users\test\test.exe" & " ", "one");
				expect(rereplace(myvar, '/','','one')).toBe("C:\Users\test\test.exe server start");
				expect(reReplaceNoCase("box server start", "^box(\.exe)? ", 'C:\\Users\test\box.exe' & ' ', 'one')).toBe("C:\/Users\test\box.exe server start");
				expect(REReplaceNoCase("This is test string","test(\.exe)? ","Replace string")).toBe("This is Replace string string");
				expect(REReplaceNoCase("This is test string","test(\.exe)? ","\UReplace string")).toBe("This is REPLACE STRING string");
				expect(REReplaceNoCase("This is test string","test(\.exe)? ","\\UReplace string")).toBe("This is \UReplace string string");
			});
		});
	}
}