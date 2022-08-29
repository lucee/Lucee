component extends="org.lucee.cfml.test.LuceeTestCase" labels="extensions"{
	function run( testResults , testBox ) {
		describe( "test case for extensionInfo()", function() {
			it(title = "Checking with extensionInfo()", body = function( currentSpec ) {
				var exts = ExtensionList();
				var ext = ExtensionInfo( exts.id[1] );
				expect( ext ).toBeStruct();
				expect( ext ).notToBeEmpty();
			});

			it(title = "Checking extensionInfo() with invalid guid returns empty struct", body = function( currentSpec ) {
				var ext = ExtensionInfo( 'not an ext guid' );
				expect( ext ).toBeStruct();
				expect( ext ).toBeEmpty();
			});

			it(title = "Checking Empty Spaces in GUID", body = function( currentSpec ) {
				var ext = ExtensionInfo( ' CED6227E-0F49-6367-A68D21AACA6B07E8 ' );
				expect( ext ).notToBeEmpty();
			});

			it(title = "Checking extensionInfo() to return correct extension Info", body = function( currentSpec ) {
				var ext = ExtensionInfo( 'CED6227E-0F49-6367-A68D21AACA6B07E8' );
				expect( ext.name ).toBe('Lucee Administrator');
			});
		});
	}
}