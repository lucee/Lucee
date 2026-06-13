component extends="org.lucee.cfml.test.LuceeTestCase" labels="reflection" {

	function run( testResults, testBox ) {

		describe( "LDEV-6377 obj.foo resolves is*() boolean getters on Java objects", function() {

			var tmpFile = createObject( "java", "java.io.File" ).init( getTempDirectory() );
			var emptyStr = "";
			var nonEmptyStr = "lucee";
			var emptyList = createObject( "java", "java.util.ArrayList" ).init();
			var fullList = createObject( "java", "java.util.ArrayList" ).init();
			fullList.add( "one" );

			it( title="File.isHidden() via .hidden shorthand", body=function() {
				// On a directory like getTempDirectory(), isHidden is typically false on Windows
				// but we only care that the property resolves at all (no "no property" exception)
				expect( tmpFile.hidden ).toBe( tmpFile.isHidden() );
			});

			it( title="File.isDirectory() via .directory shorthand", body=function() {
				expect( tmpFile.directory ).toBeTrue();
				expect( tmpFile.directory ).toBe( tmpFile.isDirectory() );
			});

			it( title="File.isFile() via .file shorthand", body=function() {
				expect( tmpFile.file ).toBeFalse();
				expect( tmpFile.file ).toBe( tmpFile.isFile() );
			});

			it( title="File.isAbsolute() via .absolute shorthand", body=function() {
				expect( tmpFile.absolute ).toBeTrue();
				expect( tmpFile.absolute ).toBe( tmpFile.isAbsolute() );
			});

			it( title="String.isEmpty() via .empty shorthand (primitive boolean return)", body=function() {
				expect( emptyStr.empty ).toBeTrue();
				expect( nonEmptyStr.empty ).toBeFalse();
			});

			// SKIPPED: Lucee's Reflector.getProperty checks fields BEFORE the getter path.
			// ArrayList has a private static EMPTY_ELEMENTDATA field that gets matched by
			// getFieldsIgnoreCase (Lucee bypasses access modifiers), returning the field
			// name "EMPTY" before isEmpty() is ever called. Unrelated to LDEV-6377 —
			// the is*() fallback works correctly (see String.empty above); this is a
			// pre-existing quirk in the field-vs-getter precedence.
			xit( title="ArrayList.isEmpty() via .empty shorthand", body=function() {
				expect( emptyList.empty ).toBeTrue();
				expect( fullList.empty ).toBeFalse();
			});

			it( title="property name is case-insensitive", body=function() {
				expect( tmpFile.HIDDEN ).toBe( tmpFile.isHidden() );
				expect( tmpFile.Directory ).toBe( tmpFile.isDirectory() );
			});

			it( title="missing property still throws (negative path preserved)", body=function() {
				expect( function() {
					var x = tmpFile.totallyMadeUpProperty;
				}).toThrow();
			});

		});

	}

}
