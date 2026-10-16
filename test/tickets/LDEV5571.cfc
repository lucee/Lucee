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

	function run( testResults, testBox ) {
		describe( "LDEV-5571: Custom Truststore for SSL Certs", function() {

			it( "SSL test server should be running", function() {
				expect( variables.ssl.isRunning() ).toBeTrue( "SSL test server failed to start" );
			});

			it( "should fail connecting to self-signed server without cert installed", function() {
				var result = {};
				cfhttp( url="https://localhost:#variables.ssl.getPort()#/", result="result", timeout=10, pooling=false );
				expect( result.error ).toBeTrue();
				expect( result.statusCode ).toInclude( "Connection Failure" );
			});

			it( "should connect using trustStore cfhttp attribute", function() {
				var result = {};
				cfhttp(
					url                = "https://localhost:#variables.ssl.getPort()#/",
					result             = "result",
					timeout            = 10,
					pooling            = false,
					trustStore         = variables.ssl.getTruststorePath(),
					trustStorePassword = variables.ssl.getTruststorePassword()
				);
				expect( result.statusCode ).toInclude( "200" );
			});

			it( "should install cert into custom-cacerts and connect without trustStore attribute", function() {
				SSLCertificateInstall( "localhost", variables.ssl.getPort() );

				var result = {};
				cfhttp( url="https://localhost:#variables.ssl.getPort()#/", result="result", timeout=10, pooling=false );
				expect( result.statusCode ).toInclude( "200" );
			});

			it( "should list installed cert via SSLCertificateList", function() {
				var certs = SSLCertificateList();
				var found = false;
				for ( var row in certs ) {
					if ( findNoCase( "localhost", row.subject ) ) {
						found = true;
						break;
					}
				}
				expect( found ).toBeTrue();
			});

			it( "should remove cert and connection fails again", function() {
				var certs = SSLCertificateList();
				var alias = "";
				for ( var row in certs ) {
					if ( findNoCase( "localhost", row.subject ) ) {
						alias = row.alias;
						break;
					}
				}
				expect( alias ).notToBeEmpty();

				SSLCertificateRemove( alias );

				var result = {};
				cfhttp( url="https://localhost:#variables.ssl.getPort()#/", result="result", timeout=10, pooling=false );
				expect( result.error ).toBeTrue();
				expect( result.statusCode ).toInclude( "Connection Failure" );
			});

			it( "should install cert via cfadmin updatesslcertificate action", function() {
				admin
					action   = "updatesslcertificate"
					type     = "server"
					password = "#request.SERVERADMINPASSWORD#"
					host     = "localhost"
					port     = "#variables.ssl.getPort()#";

				var result = {};
				cfhttp( url="https://localhost:#variables.ssl.getPort()#/", result="result", timeout=10, pooling=false );
				expect( result.statusCode ).toInclude( "200" );
			});

			it( "should list installed cert via cfadmin getallsslcertificate action", function() {
				admin
					action         = "getallsslcertificate"
					type           = "server"
					password       = "#request.SERVERADMINPASSWORD#"
					returnVariable = "local.certs";

				expect( isQuery( certs ) ).toBeTrue();
				expect( certs ).toHaveKey( "alias" );
				expect( certs ).toHaveKey( "subject" );
				expect( certs ).toHaveKey( "issuer" );

				var found = false;
				for ( var row in certs ) {
					if ( findNoCase( "localhost", row.subject ) ) {
						found = true;
						break;
					}
				}
				expect( found ).toBeTrue();
			});

			it( "should remove cert via cfadmin removesslcertificate action and connection fails again", function() {
				var certs = SSLCertificateList();
				var alias = "";
				for ( var row in certs ) {
					if ( findNoCase( "localhost", row.subject ) ) {
						alias = row.alias;
						break;
					}
				}
				expect( alias ).notToBeEmpty();

				admin
					action   = "removesslcertificate"
					type     = "server"
					password = "#request.SERVERADMINPASSWORD#"
					alias    = "#alias#";

				var result = {};
				cfhttp( url="https://localhost:#variables.ssl.getPort()#/", result="result", timeout=10, pooling=false );
				expect( result.error ).toBeTrue();
				expect( result.statusCode ).toInclude( "Connection Failure" );
			});

		});
	}

}
