component extends="org.lucee.cfml.test.LuceeTestCase" labels="ldap" {

	function run( testResults, testBox ) {
		describe( "LDEV-6141 cfldap clientCert / clientCertPassword attributes", function() {

			it( title="CFSSL_CLIENT_AUTH throws when clientCert path does not exist", body=function( currentSpec ) {
				var certPath = getTempDirectory() & "LDEV6141-missing-" & createUniqueId() & ".p12";
				expect( function() {
					cfldap(
						server="127.0.0.1",
						port=636,
						secure="CFSSL_CLIENT_AUTH",
						clientCert=certPath,
						clientCertPassword="secret",
						action="query",
						name="local.q",
						start="dc=test",
						attributes="cn"
					);
				} ).toThrow();
			} );

			it( title="CFSSL_CLIENT_AUTH throws when clientCert is not a valid PKCS12 file", body=function( currentSpec ) {
				var certPath = getTempDirectory() & "LDEV6141-invalid-" & createUniqueId() & ".p12";
				fileWrite( certPath, "not a keystore" );
				try {
					expect( function() {
						cfldap(
							server="127.0.0.1",
							port=636,
							secure="CFSSL_CLIENT_AUTH",
							clientCert=certPath,
							clientCertPassword="secret",
							action="query",
							name="local.q",
							start="dc=test",
							attributes="cn"
						);
					} ).toThrow();
				}
				finally {
					if ( fileExists( certPath ) ) fileDelete( certPath );
				}
			} );

		} );
	}

}
