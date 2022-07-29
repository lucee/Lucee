component extends="org.lucee.cfml.test.LuceeTestCase" labels="ldap"	{

	public function beforeAll(){
		variables.ldap = server.getTestService( "ldap" );
	}

	function isDisabled(){
		return ( len( server.getTestService( "ldap" ) ) eq 0 );
	}

	function run( testResults, textbox ) {
		describe(title="checking CFLDAP tag", body = function( currentSpec ) {
			it(title="ldap action=query", skip=isDisabled(), body = function( currentSpec ) {
				cfldap( server=ldap.server,
					port=ldap.port,
					username=ldap.username,
					password=ldap.password,
					action="query",
					name="local.results",
					start=ldap.base_dn,
					attributes="cn,ou" );
				systemOutput( results, true );
				expect( results.recordcount ).toBeGT( 0 );
			});

			it (title="ldap action=modify", skip=isDisabled(), body = function( currentSpec ) {
				// rename user
				cfldap( server=ldap.server,
					port=ldap.port,
					username=ldap.username,
					password=ldap.password,
					action="modify",
					dn="uid=jduke,ou=Users,dc=ldap,dc=example",
					name="local.results",
					attributes="cn=Lucee Dev" );

				// check user is renamed
				cfldap( server=ldap.server,
					port=ldap.port,
					username=ldap.username,
					password=ldap.password,
					action="query",
					name="local.results",
					start=ldap.base_dn,
					filter="uid=jduke",
					attributes="cn,ou" );

				systemOutput( results, true );
				expect( results.cn ).toBe( "Lucee Dev" );

			});

			it (title="ldap action=modifyDN", skip=true, body = function( currentSpec ) {
				// add user
				cfldap( server=ldap.server,
					port=ldap.port,
					username=ldap.username,
					password=ldap.password,
					action="add",
					dn="uid=ralio,ou=Users,dc=ldap,dc=example",
					name="local.results",
					attributes="cn=ralio"
				);

				// check user exists
				cfldap( server=ldap.server,
					port=ldap.port,
					username=ldap.username,
					password=ldap.password,
					action="query",
					name="local.results",
					start=ldap.base_dn,
					filter="uid=ralio",
					attributes="cn,ou,uid");

				expect( results.recordcount ).toBe( 1 );
				expect( results.cn ).toBe( "ralio" );

				// rename user
				cfldap( server=ldap.server,
					port=ldap.port,
					username=ldap.username,
					password=ldap.password,
					action="modifyDN",
					dn="uid=ralio,ou=Users,dc=ldap,dc=example",
					name="local.results",
					attributes="cn=Lucee" );

				// check user is renamed
				cfldap( server=ldap.server,
					port=ldap.port,
					username=ldap.username,
					password=ldap.password,
					action="query",
					name="local.results",
					start=ldap.base_dn,
					filter="uid=ralio",
					attributes="cn,ou" );

				systemOutput( results, true );
				expect( results.cn ).toBe( "Lucee" );

			});

			it (title="ldap action=add then delete", skip=isDisabled(), body = function( currentSpec ) {
				// add user
				cfldap( server=ldap.server,
					port=ldap.port,
					username=ldap.username,
					password=ldap.password,
					action="add",
					dn="uid=lucee,ou=Users,dc=ldap,dc=example",
					name="local.results",
					attributes="cn=Lucee"
				);

				// check user exists
				cfldap( server=ldap.server,
					port=ldap.port,
					username=ldap.username,
					password=ldap.password,
					action="query",
					name="local.results",
					start=ldap.base_dn,
					filter="uid=lucee",
					attributes="cn,ou,uid");

				expect( results.recordcount ).toBe( 1 );
				expect( results.cn ).toBe( "Lucee" );

				// delete user
				cfldap( server=ldap.server,
					port=ldap.port,
					username=ldap.username,
					password=ldap.password,
					action="delete",
					dn="uid=lucee,ou=Users,dc=ldap,dc=example",
					name="local.results"
				);
				
				// check user no longer exists
				cfldap( server=ldap.server,
					port=ldap.port,
					username=ldap.username,
					password=ldap.password,
					action="query",
					name="local.results",
					start=ldap.base_dn,
					filter="uid=lucee",
					attributes="cn,ou,uid");

				systemOutput( results, true );
				expect( results.recordcount ).toBe( 0 );

			});
		});
	}
	
}