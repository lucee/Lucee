component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.prefix = "t" & hash(createUniqueID());

		variables.datasource = server.getDatasource( "h2", server._getTempDir( "tag-dbinfo" ) );
		application action="update" datasource=variables.datasource;

		tableCreation();
	}

	function afterAll(){
		tableCreation(onlyDrop=true);
	}

	function run( testResults , testBox ) {

		describe( title="Test suite for DBINFO", body=function() {

			it(title = "dbinfo columns", body = function( currentSpec ) {
				dbinfo name="local.result" type= "columns" table="#variables.prefix#_users";
				//debug( result );
				expect( result.recordcount ).toBe( 3 );
				expect( queryColumnData( result, "column_name" ) ).toInclude('role_id');
				expect( listToArray( result.columnList ) ).toInclude('REFERENCED_PRIMARYKEY');

				expect( function(){
					dbinfo name="local.result" type= "columns" table="#variables.prefix#_users2";
				}).toThrow(); // table doesn't exist
			});

			it(title = "dbinfo columns wildcard", body = function( currentSpec ) {
				dbinfo name="local.result" type= "columns" table="#variables.prefix#_r%";
				//debug( result );
				expect( result.recordcount ).toBe( 2 ); // only the roles table
			});

			it(title = "dbinfo columns_minimal", body = function( currentSpec ) {
				dbinfo name="local.result" type= "columns_minimal" table="#variables.prefix#_users";
				//debug( result );
				expect( result.recordcount ).toBe( 3 );
				expect( listToArray( result.columnList ) ).notToInclude('REFERENCED_PRIMARYKEY');
			});

			it(title = "dbinfo index", body = function( currentSpec ) {
				dbinfo name="local.result" type= "index" table="#variables.prefix#_roles";
				//debug ( result );
				expect( result.recordcount ).toBe( 1 );
				expect(result.column_name ).toBe('role_id');
			});

			it(title = "dbinfo version", body = function( currentSpec ) {
				dbinfo name="local.result" type="version";
				expect( result.recordcount ).toBe( 1 );
				//debug ( result );
			});

			it(title = "dbinfo tables", body = function( currentSpec ) {
				dbinfo name="local.result" type= "tables" pattern="#variables.prefix#_users";
				expect( result.recordcount ).toBe( 1 );
				//debug(result);
			});

			it(title = "dbinfo tables, pattern filter", body = function( currentSpec ) {
				dbinfo name="local.result" type= "tables" pattern="#variables.prefix#_%";
				expect( result.recordcount ).toBe( 3 );
				//debug(result);
			});

			it(title = "dbinfo tables, only views, pattern filter", body = function( currentSpec ) {
				dbinfo name="local.result" type= "tables" pattern="#variables.prefix#_%" filter="view";
				expect( result.recordcount ).toBe( 1 );
				//debug(result);
			});

			it(title = "dbinfo tables, only tables, pattern filter", body = function( currentSpec ) {
				dbinfo name="local.result" type= "tables" pattern="#variables.prefix#_%" filter="table";
				expect( result.recordcount ).toBe( 2 );
				//debug(result);
			});

		});
	}

	private function tableCreation(onlyDrop=false){

		query {
			echo("DROP view IF EXISTS #variables.prefix#_v_users");
		}
		query {
			echo("DROP TABLE IF EXISTS #variables.prefix#_users");
		}
		query {
			echo("DROP TABLE IF EXISTS #variables.prefix#_roles");
		}

		if ( arguments.onlyDrop )
			return;

		query {
			echo("CREATE TABLE #variables.prefix#_roles (
				role_id INT(11),
				role_name VARCHAR(100) DEFAULT NULL,
				PRIMARY KEY ( role_id )
			)");
	  	}
		query {
			echo("CREATE TABLE #variables.prefix#_users (
				user_id VARCHAR(50) NOT NULL,
				user_name VARCHAR(50) NOT NULL,
				role_id INT(11) DEFAULT NULL,
				PRIMARY KEY (user_id),
				KEY role_id (role_id),
				CONSTRAINT fk_#variables.prefix#_user_role_id FOREIGN KEY ( role_id )
					REFERENCES #variables.prefix#_roles ( role_id ) ON DELETE CASCADE ON UPDATE CASCADE
			)");
		}

		query {
			echo("CREATE view #variables.prefix#_v_users as
				select 	u.user_id, u.user_name, r.role_id, r.role_name
				from 	#variables.prefix#_users u, #variables.prefix#_roles r
				where 	r.role_id = u.role_id
			");
		}
	}
}