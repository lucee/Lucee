component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {
		describe("testcase for setPropertyString Function", function() {

			beforeEach(function() {
				variables.testFilePath =  getTempFile(getTempDirectory(), "SetPropertyString", "properties");
				fileWrite( variables.testFilePath, "appName=Ralio#chr(10)#port=8080#chr(10)#smtpserver=mail.example.com" );
			});

			afterEach(function() {
				if ( fileExists( variables.testFilePath ) )
					fileDelete( variables.testFilePath );
			});

			it("retrieves the correct value for a given key", function() {
				expect( getPropertyString( variables.testFilePath, "appName" ) ).toBe( "Ralio" );
				setPropertyString( variables.testFilePath, "appName", "Lucee" );
				expect( getPropertyString( variables.testFilePath, "appName" ) ).toBe( "Lucee" );
			});

			it("throws an exception for non-existent file", function() {
				expect(function() {
					setPropertyString( variables.testFilePath & "-missing", "appName", "lucee" );
				}).toThrow();
			});

			it("respects the specified encoding", function() {
				var utf16FilePath = getTempFile( getTempDirectory(), "setPropertyStringEnc", "properties" );
				fileWrite( utf16FilePath, "key=original", "UTF-16" );

				setPropertyString( utf16FilePath, "key", "caf" & chr( 233 ), "UTF-16" );
				expect( getPropertyString( utf16FilePath, "key", "UTF-16" ) ).toBe( "caf" & chr( 233 ) );

				fileDelete( utf16FilePath );
			});
		});
	}
}