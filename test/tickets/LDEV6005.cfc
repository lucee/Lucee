component extends="org.lucee.cfml.test.LuceeTestCase" labels="cfhttp,ssl" {

	function run() {
		describe( "LDEV-6005 cfhttp trustStore and sslVerify attributes", function() {

			it( "should connect to HTTPS with default sslVerify=true", function() {
				var result = {};
				cfhttp( url="https://www.google.com", result="result", timeout=30 );
				expect( result.statusCode ).toInclude( "200" );
			});

			it( "should connect with sslVerify=false (disables cert verification)", function() {
				var result = {};
				cfhttp( url="https://www.google.com", result="result", timeout=30, sslVerify=false );
				expect( result.statusCode ).toInclude( "200" );
			});

			it( "should fail with invalid trustStore path", function() {
				expect( function() {
					var result = {};
					cfhttp( url="https://www.google.com", result="result", timeout=30, trustStore="/nonexistent/path/to/truststore.jks" );
				}).toThrow();
			});

			it( "should fail with invalid trustStore password", function() {
				// Create a temp keystore file
				var tempFile = getTempFile( getTempDirectory(), "truststore", ".jks" );
				// Write empty file - will fail to load as keystore
				fileWrite( tempFile, "" );

				expect( function() {
					var result = {};
					cfhttp( url="https://www.google.com", result="result", timeout=30, trustStore=tempFile, trustStorePassword="wrongpassword" );
				}).toThrow();

				if ( fileExists( tempFile ) ) fileDelete( tempFile );
			});

		});
	}

}
