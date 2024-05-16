component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" labels="ldap" {

	public function beforeAll(){
		variables.ldap = server.getTestService( "ldap" );
		//cleanup();
	}

	public function afterAll(){
		//cleanup();
	}

	function isDisabled(){
		return ( len( server.getTestService( "ldap" ) ) eq 0 );
	}

	
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2622", function() {
			it( title = "Checking SECURE LDAP Connection, secure=CFSSL_BASIC", body=function( currentSpec ) {
				cfldap( server=ldap.server,
					port=ldap.port_secure,
					timeout=5000,
					username=ldap.username,
					password=ldap.password,
					secure="CFSSL_BASIC",
					action="query",
					name="local.results",
					start=ldap.base_dn,
					filter="(objectClass=inetOrgPerson)",
					attributes="cn" 
				);
				expect( results.recordcount ).toBeGT( 0 );
			});

			it( title = "Checking SECURE LDAP Connection, useTls=true", body=function( currentSpec ) {
				cfldap( server=ldap.server,
					port=ldap.port_secure,
					timeout=5000,
					username=ldap.username,
					password=ldap.password,
					useTls=true,
					action="query",
					name="local.results",
					start=ldap.base_dn,
					filter="(objectClass=inetOrgPerson)",
					attributes="cn" 
				);
				expect( results.recordcount ).toBeGT( 0 );
			});

		});
	}
} 
