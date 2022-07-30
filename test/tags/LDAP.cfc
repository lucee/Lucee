component extends="org.lucee.cfml.test.LuceeTestCase" labels="ldap"	{

	// uses docke image form docker pull rroemhild/test-openldap

	public function beforeAll(){
		variables.ldap = server.getTestService( "ldap" );
	}

	function isDisabled(){
		return ( len( server.getTestService( "ldap" ) ) eq 0 );
	}

	function run( testResults, textbox ) {
		describe(title="checking CFLDAP tag", body = function( currentSpec ) {
			it(title="ldap action=query", skip=isDisabled(), body = function( currentSpec ) {
				// firstly do we have some data in LDAP
				var results = ldapQuery( start=ldap.base_dn );
				expect( results.recordcount ).toBeGT( 0 );
			});

			it (title="ldap action=modify", skip=isDisabled(), body = function( currentSpec ) {
				// create a user
				ldapAdd( dn="uid=ralio,ou=people,#ldap.base_dn#", 
					attributes=ldapAddAttributes( cn="Ralio", sn="Fusion", uid="ralio" ) );

				// update user
				ldapUpdate( dn="uid=ralio,ou=people,#ldap.base_dn#", attributes="cn=Lucee Dev");

				// check user is renamed
				var results = ldapQuery( start=ldap.base_dn, filter="(cn=Lucee Dev)" );
				expect( results.recordcount ).toBe( 1 );
				expect( results.cn ).toBe( "Lucee Dev" );
			});

			it (title="ldap action=modifyDN", skip=true, body = function( currentSpec ) {
				// add user
				ldapAdd( dn="uid=cfml,ou=people,#ldap.base_dn#", 
					attributes=ldapAddAttributes( cn="Cold Fusion", sn="Fusion", uid="cfml" ) );

				// check user exists
				var results = ldapQuery(start=ldap.base_dn, filter="(cn=cfml)");
				expect( results.recordcount ).toBe( 1 );
				expect( results.cn ).toBe( "Cold fusion" );

				// rename user
				ldapRename( dn="uid=cfml,ou=people,#ldap.base_dn#", attributes="cn=Lucee");

				// check user is renamed
				var results = ldapQuery(start=ldap.base_dn,	filter="(cn=Lucee)" );
				expect( results.cn ).toBe( "Lucee" );

			});

			it (title="ldap action=add then delete", skip=isDisabled(), body = function( currentSpec ) {
				// add user
				ldapAdd( dn="uid=Lucee,ou=people,#ldap.base_dn#",
					attributes=ldapAddAttributes( cn="Lucee", sn="6.0", uid="Lucee" ) );

				// check user exists
				var results = ldapQuery(start=ldap.base_dn, filter="(cn=lucee)");
				expect( results.recordcount ).toBe( 1 );
				expect( results.cn ).toBe( "Lucee" );

				// delete user
				ldapDelete( dn="uid=lucee,ou=people,#ldap.base_dn#" );
				
				// check user no longer exists
				var results = ldapQuery(start=ldap.base_dn, filter="(cn=lucee)");
				expect( results.recordcount ).toBe( 0 );

			});
		});
	}
	
	private function ldapQuery(
			string start=ldap.base_dn, 
			string filter="(objectClass=inetOrgPerson)", 
			string attributes="cn,ou,uid" ) {
		cfldap( server=ldap.server,
			port=ldap.port,
			username=ldap.username,
			password=ldap.password,
			action="query",
			name="local.results",
			start=arguments.start,
			filter=arguments.filter,
			attributes=arguments.attributes);
		// systemOutput( results, true );
		return results;
	}

	private function ldapAdd(
			required string dn, 
			required string attributes) {
		cfldap( server=ldap.server,
			port=ldap.port,
			username=ldap.username,
			password=ldap.password,
			action="add",
			dn=arguments.dn,
			attributes=arguments.attributes
		);

	}

	private function ldapDelete(
			required string dn ) {
		cfldap( server=ldap.server,
			port=ldap.port,
			username=ldap.username,
			password=ldap.password,
			action="delete",
			dn=arguments.dn
		);
	}

	private function ldapRename(
			required string dn,
			required attributes ){
		cfldap( server=ldap.server,
			port=ldap.port,
			username=ldap.username,
			password=ldap.password,
			action="modifyDN",
			dn=arguments.dn,
			attributes=arguments.attributes 
		);
	}

	private function ldapUpdate(
			required string dn,
			required attributes ){
		cfldap( server=ldap.server,
			port=ldap.port,
			username=ldap.username,
			password=ldap.password,
			action="modify",
			dn=arguments.dn,
			attributes=arguments.attributes 
		);
	}

	private function ldapAddAttributes(cn, sn, uid){
		return "objectclass=inetOrgPerson;"
			& "cn=#trim(arguments.cn)#;" 
			& "sn=#trim(arguments.sn)#;"
			& "uid=#trim(arguments.uid)#";
		// mail=#Trim(Form.email)#; 
		// telephonenumber=#Trim(Form.phone)#; 
		// ou=Human Resources; 
	}

}