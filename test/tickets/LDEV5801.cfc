component extends="org.lucee.cfml.test.LuceeTestCase" labels="jdbc" {

	function beforeAll() {
		if (!structKeyExists(server, "getDatasource")) {
			// for testing via browser
			variables.postgres = {
				"bundleName":"org.postgresql.jdbc",
				"bundleVersion":"42.7.7",
				"password":"lucee",
				"connectionString":"jdbc:postgresql://localhost:15432/lucee",
				"class":"org.postgresql.Driver",
				"username":"lucee"
			};
			variables.postgresConfig = {
				"host":"localhost",
				"port":"15432",
				"database":"lucee"
			};
			request.serverAdminPassword = "webweb";
		} else {
			variables.postgres = server.getDatasource(service="postgres");
			variables.postgresConfig = server.getDatasource(service="postgres", onlyConfig=true);
		}
		variables.tmpConfigDatasources = {};
	}

	function afterAll(){
		structEach( variables.tmpConfigDatasources, function( key ) {
			cfadmin (action="removeDatasource",	type="server",password="#request.serverAdminPassword#",name="#key#");
		});
	}

	function run( testResults , testBox ) {

		describe( title="LDEV-5801 check exceptions via datasource struct", body=function() {

			it(title="call dbinfo with valid datasource", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = variables.postgres;
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should not throw an exception" );
				expect( result ).toBeQuery();
			});

			it(title="call dbinfo with valid datasource [only class]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "class" ] = variables.postgres.class;
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should not throw an exception" );
				expect( result ).toBeQuery();
			});

			it(title="call dbinfo with valid datasource [only invalid class]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "class" ] = variables.postgres.class & "zzzz";
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should throw an exception" );
				expect( error.message ).toInclude( ds.class );
			});

			xit(title="call dbinfo with valid datasource [type]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "type" ] = "PostgreSQL";
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					//  key [class] doesn't exist
					debug(e);
					error = e;
				}
				//expect( error ).toBe( "", "should not throw an exception" );
				expect( error.message ).notToInclude( "specified name [null] could be found" );
				expect( error.message ).notToInclude( "key [class] doesn't exist" );

			});

			xit(title="call dbinfo with invalid datasource [dbdriver]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "dbdriver" ] = "PostgreSQL";
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					// key [class] doesn't exist
					debug(e);
					error = e;
				}
				// expect( error ).notToBe( "", "should not throw an exception" );
				expect( error.message ).notToInclude( "specified name [null] could be found" );
				expect( error.message ).notToInclude( "key [class] doesn't exist" );
			});

			xit(title="call dbinfo with invalid datasource, no dbdriver or type", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					//key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should throw an exception" );
				expect( error.message ).notToInclude( "specified name [null] could be found" );
				expect( error.message ).notToInclude( "key [class] doesn't exist" );
			});
		});

		describe( title="LDEV-5801 check exceptions via config datasource", body=function() {

			it(title="call dbinfo with valid datasource", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = duplicate(variables.postgres);
				ds = createConfigDatasource(ds, "default");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should not throw an exception" );
				expect( result ).toBeQuery();
			});

			it(title="call dbinfo with valid datasource [only class]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "class" ] = variables.postgres.class;
				ds = createConfigDatasource(ds, "only_class");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should not throw an exception" );
				expect( result ).toBeQuery();
			});

			it(title="call dbinfo with valid datasource [only invalid class]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "class" ] = variables.postgres.class & "zzzz";
				ds = createConfigDatasource(ds, "only_class");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should throw an exception" );
				expect( error.message ).toInclude( "cannot load class through its string name" );
			});

			it(title="call dbinfo with valid datasource [type]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "type" ] = "PostgreSQL";
				ds = createConfigDatasource(ds, "only_type");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					//  key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).toBe( "", "should not throw an exception" );
				expect( result ).toBeQuery();
			});

			xit(title="call dbinfo with valid datasource invalid [type] ", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "type" ] = "PostgreSQLInvalid";
				ds = createConfigDatasource(ds, "only_type_invalid");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					//  key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should throw an exception" );
				expect(	error.message ).notToInclude( "specified name [null] could be found" );
			});

			xit(title="call dbinfo with datasource [dbdriver]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "dbdriver" ] = "PostgreSQL";
				ds = createConfigDatasource( ds, "only_dbdriver" );
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					// key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should not throw an exception" );
				expect(	error.message ).notToInclude( "specified name [null] could be found" );
			});

			xit(title="call dbinfo with datasource with invalid [dbdriver]", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = getDatatasourceWithoutClass();
				ds[ "dbdriver" ] = "PostgreSQLInvalid";
				ds = createConfigDatasource( ds, "only_dbdriver_invalid" );
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					// key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should throw an exception" );
				expect(	error.message ).notToInclude( "specified name [null] could be found" );
			});

			xit(title="call dbinfo with invalid datasource, no dbdriver or type", skip=isPostgresNotAvailable(), body = function( currentSpec ) {
				var ds = createConfigDatasource(getDatatasourceWithoutClass(), "no_dbdriver_type");
				var error = "";
				var result = "";
				try {
					dbinfo datasource="#ds#" name="result" type="dbnames";
				} catch (any e) {
					//key [class] doesn't exist
					debug(e);
					error = e;
				}
				expect( error ).notToBe( "", "should throw an exception" );
				expect(	error.message ).notToInclude( "specified name [null] could be found" );
			});
		});

		describe( title="LDEV-5801 datasource without any JDBC driver class (no database needed)", body=function() {

			// the exception is thrown before any connection is made, so no database is needed
			it(title="struct datasource with only [dbdriver], no [type] or [class]", body = function( currentSpec ) {
				var ds = getDriverlessDatasource();
				ds[ "dbdriver" ] = "PostgreSQL";
				var e = getDbinfoError( ds );
				expectNoDriverError( e, "" );
			});

			it(title="struct datasource with neither [type], [class] nor [dbdriver]", body = function( currentSpec ) {
				var e = getDbinfoError( getDriverlessDatasource() );
				expectNoDriverError( e, "" );
			});

			it(title="struct datasource with [connectionString] but without [class]", body = function( currentSpec ) {
				var ds = getDriverlessDatasource();
				ds[ "connectionString" ] = "jdbc:postgresql://localhost:15432/lucee";
				var e = getDbinfoError( ds );
				expectNoDriverError( e, "" );
			});

			it(title="struct datasource with invalid [type]", body = function( currentSpec ) {
				var ds = getDriverlessDatasource();
				ds[ "type" ] = "PostgreSQLInvalid";
				var e = getDbinfoError( ds );
				expect( e.message ).toInclude( "PostgreSQLInvalid" );
				expect( e.message ).notToInclude( "[null]" );
			});

			it(title="config datasource with only [dbdriver], no [type] or [class]", body = function( currentSpec ) {
				var ds = createDriverlessConfigDatasource( "only_dbdriver_nodb", { "dbdriver": "PostgreSQL" } );
				var e = getDbinfoError( ds );
				expectNoDriverError( e, ds );
				expect( e.message ).toInclude( "dbdriver" );
			});

			it(title="config datasource with neither [dbdriver], [type] nor [class]", body = function( currentSpec ) {
				var ds = createDriverlessConfigDatasource( "nothing_nodb", {} );
				var e = getDbinfoError( ds );
				expectNoDriverError( e, ds );
			});

			it(title="config datasource with invalid [type]", body = function( currentSpec ) {
				var ds = createDriverlessConfigDatasource( "invalid_type_nodb", { "type": "PostgreSQLInvalid" } );
				var e = getDbinfoError( ds );
				expectNoDriverError( e, ds );
			});
		});

	}

	private function getDriverlessDatasource() {
		return {
			"host": "localhost",
			"port": "15432",
			"database": "lucee",
			"username": "lucee",
			"password": "lucee"
		};
	}

	private function createDriverlessConfigDatasource( required string name, required struct extra ) {
		var dsn = "ldev5801-" & arguments.name;
		var _ds = getDriverlessDatasource();
		structAppend( _ds, arguments.extra );
		configImport( { "datasources": { "#dsn#": _ds } }, "server", request.serverAdminPassword );
		tmpConfigDatasources[ dsn ] = true;
		return dsn;
	}

	private function getDbinfoError( required any ds ) {
		try {
			dbinfo datasource="#arguments.ds#" name="local.result" type="dbnames";
		}
		catch ( any e ) {
			return e;
		}
		fail( "dbinfo should throw an exception for a datasource without a JDBC driver" );
	}

	private function expectNoDriverError( required any e, required string dsName ) {
		expect( e.message ).notToInclude( "[null]" );
		expect( e.message ).notToInclude( "cannot load class through its string name" );
		expect( e.message ).notToInclude( "doesn't exist" );
		expect( e.message ).toInclude( "class" );
		expect( e.message ).toInclude( "type" );
		if ( len( arguments.dsName ) ) expect( e.message ).toInclude( arguments.dsName );
	}

	private function createConfigDatasource( ds, name ) {
		var dsn = "ldev5801-" & name;
		var config = {
			"datasources": {}
		};
		var _ds = duplicate( ds );
		structAppend( _ds, variables.postgresConfig );
		config.datasources[ dsn ] = _ds;
		configImport( config, "server", request.serverAdminPassword );
		tmpConfigDatasources[ dsn ] = true;
		return dsn;
	}

	private function getDatatasourceWithoutClass(){
		var remove = [ "class", "bundleName", "bundleVersion" ];
		var ds = duplicate( variables.postgres );
		ArrayEach( remove, function( key ) {
			structDelete( ds, key );
		});
		return ds;
	}

	private function isPostgresNotAvailable() {
		return structCount(variables.postgres?:{}) == 0;
	}

}
