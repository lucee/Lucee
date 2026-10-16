component extends="org.lucee.cfml.test.LuceeTestCase" labels="cfhttp,ssl" {

	variables.ssl = nullValue();

	function beforeAll() {
		variables.ssl = new test.tools.SSLTestServer();
		variables.ssl.start();
	}

	function afterAll() {
		if ( !isNull( variables.ssl ) ) variables.ssl.stop();
		variables.ssl = nullValue();
	}

	private string function sslURL() {
		return "https://localhost:#variables.ssl.getPort()#/";
	}

	function run() {
		describe( "LDEV-6005 cfhttp trustStore and sslVerify attributes", function() {

			it( "fails against a self-signed server with the default sslVerify=true", function() {
				var result = {};
				cfhttp( url=sslURL(), result="result", timeout=10, pooling=false );
				expect( result.statusCode ).toInclude( "Connection Failure" );
			});

			it( "connects to a self-signed server with sslVerify=false", function() {
				var result = {};
				cfhttp( url=sslURL(), result="result", timeout=10, pooling=false, sslVerify=false );
				expect( result.statusCode ).toInclude( "200" );
			});

			it( "fails with an invalid trustStore path", function() {
				expect( function() {
					var result = {};
					cfhttp( url=sslURL(), result="result", timeout=10, pooling=false, trustStore=getTempDirectory() & "ldev6005-missing-#createUUID()#.jks" );
				}).toThrow();
			});

			it( "fails with a trustStore that is not a keystore", function() {
				var tempFile = getTempFile( getTempDirectory(), "truststore", ".jks" );
				fileWrite( tempFile, "" );
				try {
					expect( function() {
						var result = {};
						cfhttp( url=sslURL(), result="result", timeout=10, pooling=false, trustStore=tempFile, trustStorePassword="wrongpassword" );
					}).toThrow();
				}
				finally {
					if ( fileExists( tempFile ) ) fileDelete( tempFile );
				}
			});

		});
	}

}
