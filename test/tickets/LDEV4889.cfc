component extends="org.lucee.cfml.test.LuceeTestCase" {


	function beforeAll(){
		if ( isMySqlNotSupported() ) return;
		variables.default = {
			"datasources": {
				"LDEV4889" : mySqlCredentials()
			}
		};

		var creds = mySqlCredentials( true );

		variables.default["datasources"].LDEV4889 = {
			"host":     creds.server,
			"type":     "MySQL",
			"dsn":      "jdbc:mysql://{host}:{port}/{database}",
			"class":    "com.mysql.cj.jdbc.Driver",
			"database": creds.database,
			"port":     creds.port,
			"username": creds.username,
			"password": creds.password
		};
	//	variables.default.datasources.LDEV4889.database = mySqlCredentials( true ).database;
	//	variables.default.datasources.LDEV4889.dsn = "jdbc:mysql://{host}:{port}/{database}";
//		variables.default.datasources.LDEV4889.dbdriver = "mysql";
	};


	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4889 - npe with .cfconfig.json datasources", function() {
			it( title="check good configuration", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var result = testConfigImport( variables.default, "good" );
				expect( result ).toBeStruct();
			});

			/*  lucee.runtime.exp.NativeException: Cannot invoke "String.length()" because "str" is null
				at java.base/java.lang.AbstractStringBuilder.<init>(AbstractStringBuilder.java:118)
				at java.base/java.lang.StringBuilder.<init>(StringBuilder.java:131)
				at lucee.runtime.db.DataSourceSupport.id(DataSourceSupport.java:396)
			*/
			xit( title="check missing dsn", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var cfg = duplicate( variables.default );
				structDelete( cfg.datasources.LDEV4889, "dsn");
				var result =  testConfigImport( cfg, "missing dsn" );
				expect( result ).toBeStruct();
			});

			/*java.lang.NullPointerException:Cannot invoke "String.trim()" because "className" is null
				at lucee.commons.lang.ExceptionUtil.toIOException(ExceptionUtil.java:213)
				at lucee.runtime.db.DatasourceConnectionFactory.create(DatasourceConnectionFactory.java:64)
				at lucee.runtime.db.DatasourceConnectionFactory.create(DatasourceConnectionFactory.java:21)
			*/
			xit( title="check missing class", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var cfg = duplicate( variables.default );
				structDelete( cfg.datasources.LDEV4889, "class");
				var result = testConfigImport(  cfg, "missing class" );
				expect( result ).toBeStruct();
			});

			it( title="check missing username", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var cfg = duplicate( variables.default );
				structDelete( cfg.datasources.LDEV4889, "username" );
				try {
					var result = testConfigImport(  cfg, "missing username" );
				} catch ( e ){
					expect( e.stackTrace ).toInclude( "java.sql.SQLException" );
				}
			});

			it( title="check missing password", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var cfg = duplicate( variables.default );
				structDelete( cfg.datasources.LDEV4889, "password" );
				try {
					var result = testConfigImport(  cfg, "missing password" );
				} catch ( e ){
					expect( e.stackTrace ).toInclude( "java.sql.SQLException" );
				}
			});

			it( title="check missing port", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var cfg = duplicate( variables.default );
				structDelete( cfg.datasources.LDEV4889, "port" );
				try {
					var result = testConfigImport(  cfg, "missing username" );
				} catch ( e ){
					expect( e.stackTrace ).toInclude( "CommunicationsException" );
				}
			});

			it( title="check missing type", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var cfg = duplicate( variables.default );
				structDelete( cfg.datasources.LDEV4889, "type" );
				var result = testConfigImport(  cfg, "missing type" );
				expect( result ).toBeStruct();
			});

			it( title="check dbdriver instead of type", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var cfg = duplicate( variables.default );
				variables.default.datasources.LDEV4889.dbdriver = variables.default.datasources.LDEV4889.type;
				structDelete( cfg.datasources.LDEV4889, "type" );
				var result = testConfigImport(  cfg, "dbdriver instead of type" );
				expect( result ).toBeStruct();
			});

			it( title="check missing type and dbdriver", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var cfg = duplicate( variables.default );
				structDelete( cfg.datasources.LDEV4889, "type" );
				structDelete( cfg.datasources.LDEV4889, "dbdriver" );
				var result = testConfigImport(  cfg, "no type or dbdriver" );
				expect( result ).toBeStruct();
			});
		}); 
	}

	private function testConfigImport( struct cfg, string desc ){
		//systemOutput( "", true );
		var id = desc.replaceNoCase(" ","_", "all");

		loop list="server" item="local.type" { // TODO "web" doesn't work in single mode, should be ignored??
			var _cfg = duplicate( cfg );
			var ds = "LDEV_4889_#type#_#id#";
			_cfg.datasources[ ds ] = duplicate( _cfg.datasources.LDEV4889 );
			structDelete( _cfg.datasources, "LDEV4889" );
		//	systemOutput( "configImport Datasource: #ds#", true );
		//	systemOutput( _cfg, true );
			var result = configImport(
				type: type,
				path: _cfg,
				password=request.SERVERADMINPASSWORD
			);
		//	systemOutput( result, true );
			dbinfo type="Version" datasource="#ds#" name="local.verify";
		}
		return local.result;
	}


	function isMySqlNotSupported() {
		return isEmpty( mySqlCredentials() );
	}

	private struct function mySqlCredentials( onlyConfig=false ) {
		return server.getDatasource( service="mysql", onlyConfig=arguments.onlyConfig );
	}

}
