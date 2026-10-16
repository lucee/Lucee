component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {
		describe("testcase for getPropertyString Function", function() {

			beforeEach(function() {
				variables.testFilePath =  getTempFile(getTempDirectory(), "getPropertyString", "properties");
				variables.props = {
					"appName": "Lucee",
					"port": 8888,
					"smtpServer": "mail.example.com"
				};
				var propFile = structReduce( props, function( result, key, value ){
					return arguments.result & arguments.key & "=" & arguments.value & chr( 10 );
				});
				fileWrite( variables.testFilePath, propFile );
			});

			afterEach(function() {
				if (fileExists( variables.testFilePath ) )
					fileDelete( variables.testFilePath );
			});

			it("retrieves the correct value for a given key", function() {
				structEach( props, function( k, v ){
					var str = getPropertyString( variables.testFilePath, k );
					expect( str ).toBe( v );
				});
			});

			it("returns an empty string for non-existent keys", function() {
				expect( getPropertyString( variables.testFilePath, "nonExistentKey") ).toBe( "" );
			});

			it("throws an exception for non-existent file", function() {
				expect(function() {
					getPropertyString( variables.testFilePath & -"missing", "somekey" );
				}).toThrow();
			});

			it("respects the specified encoding", function() {
				var utf16FilePath = getTempFile( getTempDirectory(), "getPropertyStringEnc", "properties" );
				fileWrite( utf16FilePath, "key=caf" & chr( 233 ), "UTF-16" );

				expect( getPropertyString( utf16FilePath, "key", "UTF-16" ) ).toBe( "caf" & chr( 233 ) );

				fileDelete( utf16FilePath );
			});
		});
	}
}