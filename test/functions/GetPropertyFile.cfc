component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {
		describe("testcase for getPropertyFile Function", function() {

			beforeEach(function() {
				variables.testFilePath =  getTempFile(getTempDirectory(), "getPropertyString", "properties");
				variables.props = {
					"appName": "Lucee",
					"port": 8888,
					"smtpServer": "mail.example.com"
				};
				var propFile= structReduce( props, function( result, key, value ){
					return arguments.result & arguments.key & "=" & arguments.value & chr( 10 );
				});
				fileWrite( variables.testFilePath, propFile );
			});

			afterEach(function() {
				if (fileExists( variables.testFilePath ) )
					fileDelete( variables.testFilePath );

			});

			it("retrieves the correct value for a given key", function() {
				var st = getPropertyFile( variables.testFilePath );
				structEach( props, function( k, v ){
					expect( st ).toHaveKey( k );
					expect( st[ k ]).toBe( v );
				});
			});

			it("throws an exception for non-existent file", function() {
				expect( function() {
					getPropertyFile( variables.testFilePath & "-missing", "someKey" );
				}).toThrow();
			});

			it("respects the specified encoding", function() {
				var utf16FilePath = getTempFile( getTempDirectory(), "getPropertyFileEnc", "properties" );
				fileWrite( utf16FilePath, "key=caf" & chr( 233 ), "UTF-16" );

				var st = getPropertyFile( utf16FilePath, "UTF-16" );
				expect( st ).toHaveKey( "key" );
				expect( st[ "key" ] ).toBe( "caf" & chr( 233 ) );

				fileDelete( utf16FilePath );
			});
		});
	}
}