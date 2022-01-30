component extends="org.lucee.cfml.test.LuceeTestCase"  labels="mysql" {
	//skip closure
	function isNotSupported(){
		variables.mySQL= getCredentials();
		// Admin password
		variables.adminPassword = request.WEBADMINPASSWORD;
		
		return structisEmpty(variables.mySQL);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1445", skip=isNotSupported(),  body=function() {
			it(title="checking connectionLimit set to default", body = function( currentSpec ) {
				dsnname = 'testdsn';
				dbhost = '#mySQL.server#';
				dbname = '#mySQL.database#';
				dbuser = '#mySQL.username#';
				dbpwd = '#mySQL.password#';
				adminpwd = 'password';
				args = {};
				args['connectionLimit'] = -1;
				args['Action'] = 'updateDatasource';
				args['newName'] = "testdsn";
				args['name'] = dsnname;
				args['host'] = dbhost;
				args['database'] = dbname;
				args['dbusername'] = dbuser;
				args['dbpassword'] = dbpwd;
				args['port'] = 3306;
				args['clob'] = true;
				args['blob'] = false;
				args['connectionTimeout'] = 1;
				args['validate'] = true;
				args['dsn'] = 'jdbc:mysql://localhost:3306/#mySQL.database#';
				args['classname'] = "org.gjt.mm.mysql.Driver";
				args['custom'] = {};
				args['custom']['useOldAliasMetadataBehavior'] = true;
				args['custom']['zeroDateTimeBehavior'] = 'convertToNull';
				args['custom']['characterEncoding'] = 'UTF-8';
				args['custom']['useUnicode'] = true;
				args['custom']['useLegacyDatetimeCode'] = true;
				args.type = 'web';
				args.password = adminPassword;

				admin attributeCollection="#args#" ;
				admin action="getDatasource" type="web" password="#adminPassword#" name="testdsn" returnVariable="local.rtn";
				var result = rtn.connectionLimit;
				expect(rtn.connectionLimit).toBe(-1);
			});
		});
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}
