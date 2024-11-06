component extends="org.lucee.cfml.test.LuceeTestCase" {

	variables.prefix = "dBinfo_" & left(lcase(hash(createUniqueID())), 15 ) & "_";
	variables._datasources = configureDatasources();

	function afterAll(){
		loop collection=variables._datasources key="local.k" value="local.v"{
		createSchema( ds=local.v, dbtype=local.k, prefix= prefix, onlyDrop=true );
		}
	};

	private struct function configureDatasources(){
		var datasources = [
			mysql = server.getDatasource( "mysql" ),
			oracle = server.getDatasource( "oracle" ),
			mssql = server.getDatasource( "mssql" ),
			h2 = server.getDatasource( "h2", server._getTempDir( "tag-dbinfo-h2" ) ),
			hsqldb = server.getDatasource( "hsqldb", server._getTempDir( "tag-dbinfo-hsqldb" ) ),
			postgres: server.getDatasource( "postgres" )
		];
		
		datasources = structFilter( datasources, function( k, v ){
			return !isEmpty( arguments.v );
		});
		return datasources;
	};

	function run( testResults , testBox ) {
		var datasources = variables._datasources;
		loop collection=datasources key="local.dbType" value="local.ds" {

			createSchema( ds, dbtype, prefix );

			describe( title="Test suite for DBINFO, db: [#dbType#]", body=function() {
				it(title = "dbinfo columns [#dbType#]",
						data = { prefix: prefix, ds: ds, dbtype: dbtype },
						body = function( data ) {

					dbinfo datasource=data.ds name="local.result" type= "columns" table="#data.prefix#users";
					//debug( result );
					expect( result.recordcount ).toBe( 3 );
					expect( queryColumnData( result, "column_name" ) ).toInclude('role_id');
					expect( listToArray( result.columnList ) ).toInclude('REFERENCED_PRIMARYKEY');

					expect( function(){
						dbinfo datasource=data.ds name="local.result" type= "columns" table="#prefix#_users2";
					}).toThrow(); // table doesn't exist
				});

				it(title = "dbinfo columns wildcard [#dbType#]",
						data = { prefix: prefix, ds: ds, dbtype: dbtype },
						body = function( data ) {
					dbinfo datasource=data.ds name="local.result" type= "columns" table="#data.prefix#r%";
					//debug( result );
					expect( result.recordcount ).toBe( 2 ); // only the roles table
				});

				it(title = "dbinfo columns_minimal [#dbType#]",
						data = { prefix: prefix, ds: ds, dbtype: dbtype },
						body = function( data ) {
					dbinfo datasource=data.ds name="local.result" type= "columns_minimal" table="#data.prefix#users";
					//debug( result );
					expect( result.recordcount ).toBe( 3 );
					expect( listToArray( result.columnList ) ).notToInclude('REFERENCED_PRIMARYKEY');
				});

				it(title = "dbinfo index [#dbType#]",
						data = { prefix: prefix, ds: ds, dbtype: dbtype },
						body = function( data ) {
					dbinfo datasource=data.ds name="local.result" type= "index" table="#data.prefix#roles";
					//debug ( result );
					//systemOutput( data.dbtype & " index: " & queryColumnData(result, "index_name" ).toJson(), true );
					switch (data.dbtype) {
						case "oracle":
							expect( result.recordcount ).toBe( 2 ); // extra ""
							break;
						case "mssql":
							expect( result.recordcount ).toBe( 2 ); // extra ""
							break;
						default:
							expect( result.recordcount ).toBe( 1 );
					}
					expect( queryColumnData( result, "column_name" ) ).toInclude( 'role_id' );
				});

				it(title = "dbinfo version [#dbType#]",
						data = { prefix: prefix, ds: ds, dbtype: dbtype },
						body = function( data ) {
					dbinfo datasource=data.ds name="local.result" type="version";
					expect( result.recordcount ).toBe( 1 );
					//debug ( result );
				});
				it(title = "dbinfo tables [#dbType#]",
						data = { prefix: prefix, ds: ds, dbtype: dbtype },
						body = function( data ) {
					if (data.dbtype neq "oracle") {  // oracle is super slow with a filter
						dbinfo datasource=data.ds name="local.result" type="tables";
						expect( result.recordcount ).toBeGT( 2 );
					}
					//debug(result);
				});

				it(title = "dbinfo tables, extact pattern [#dbType#]",
						data = { prefix: prefix, ds: ds, dbtype: dbtype },
						body = function( data ) {
							debug(data);
					dbinfo datasource=data.ds name="local.result" type="tables" pattern="#data.prefix#users%";
					//systemOutput( data.dbType & " tables exact pattern: " & queryColumnData(result, "table_name" ).toJson(), true )
					expect( result.recordcount ).toBe(  1 );
					debug(result);

				});

				it(title = "dbinfo tables, pattern [#dbType#]",
						data = { prefix: prefix, ds: ds, dbtype: dbtype },
						body = function( data ) {
					dbinfo datasource=data.ds name="local.result" type="tables" pattern="#data.prefix#%";
					//systemOutput( data.dbType & ": tables pattern " & queryColumnData(result, "table_name" ).toJson(), true )
					expect( result.recordcount ).toBe( 3 );
					//debug(result);
				});

				it(title = "dbinfo tables, pattern & filter=view [#dbType#]",
						data = { prefix: prefix, ds: ds, dbtype: dbtype },
						body = function( data ) {
					dbinfo datasource=data.ds name="local.result" type="tables" pattern="#data.prefix#%" filter="view";
					expect( result.recordcount ).toBe( 1 );
					//debug(result);
				});

				it(title = "dbinfo tables, pattern, filter=table [#dbType#]",
						data = { prefix: prefix, ds: ds, dbtype: dbtype },
						body = function( data ) {
					dbinfo datasource=ds name="local.result" type="tables" pattern="#data.prefix#%" filter="table";
					expect( result.recordcount ).toBe( 2 );
					//debug(result);
				});

				it(title = "dbinfo tables, pattern, filter=invalid_table_type_filter [#dbType#]",
						data = { prefix: prefix, ds: ds, dbtype: dbtype },
						body = function( data ) {
					expect(function(){
						dbinfo datasource=ds name="local.result" type="tables" pattern="#data.prefix#%" filter="invalid_table_type_filter";
					}).toThrow();
				});
			});

		}
	};

	private void function createSchema( struct ds, string dbType, string prefix, boolean onlyDrop=false ){

		systemOutput(arguments, true);

		function quoteIfNeeded( str, dbtype ){
			if (arguments.dbtype != "postgres") // TODO check metadata for supportsMixedCaseQuotedIdentifiers?
				return arguments.str;
			else
				return '"' & arguments.str & '"';
		}

		if (arguments.dbtype eq "oracle") { // oracle doesn't support the IF EXISTS syntax
			try {
				query datasource=arguments.ds {
					echo("DROP view #arguments.prefix#v_users");
				}
			} catch(e){
				//
			}
			try {
				query datasource=arguments.ds {
					echo("DROP TABLE #arguments.prefix#users");
				}
			} catch(e){
				//
			}
			try {
				query datasource=arguments.ds {
					echo("DROP TABLE #arguments.prefix#roles");
				}
			} catch(e){
				//
			}
		} else {
			query datasource=arguments.ds {
				echo('DROP view IF EXISTS #quoteIfNeeded("#arguments.prefix#v_users", arguments.dbtype)# ');
			}
			query datasource=arguments.ds {
				echo('DROP TABLE IF EXISTS #quoteIfNeeded("#arguments.prefix#users", arguments.dbtype)# ');
			}
			query datasource=arguments.ds {
				echo('DROP TABLE IF EXISTS #quoteIfNeeded("#arguments.prefix#roles", arguments.dbtype)# ');
			}
		}

		if ( arguments.onlyDrop )
			return;

		query datasource=arguments.ds {
			echo('CREATE TABLE #quoteIfNeeded("#arguments.prefix#roles", arguments.dbtype)# (
				role_id INT,
				role_name VARCHAR(100) DEFAULT NULL,
				CONSTRAINT PK_#arguments.prefix#roles PRIMARY KEY ( role_id )
			)');
	  	}
		query datasource=arguments.ds {
			echo('CREATE TABLE #quoteIfNeeded("#arguments.prefix#users", arguments.dbtype)# (
				user_id VARCHAR(50) NOT NULL,
				user_name VARCHAR(50) NOT NULL,
				role_id INT DEFAULT NULL,
				CONSTRAINT PK_#arguments.prefix#users PRIMARY KEY ( user_id )
			)');
		}
		query datasource=arguments.ds {
			echo('ALTER TABLE #quoteIfNeeded("#arguments.prefix#users", arguments.dbtype)#
					ADD CONSTRAINT fk_#arguments.prefix#_user_role_id
					FOREIGN KEY (role_id)
					REFERENCES #quoteIfNeeded("#arguments.prefix#roles", arguments.dbtype)# ( role_id )
			');
		}
		query datasource=arguments.ds {
			echo('CREATE INDEX idx_#arguments.prefix#_users_role_id 
				ON #quoteIfNeeded("#arguments.prefix#users", arguments.dbtype)#(role_id)
			');
		}

		query datasource=arguments.ds {
			echo('CREATE VIEW #quoteIfNeeded("#arguments.prefix#v_users")# AS
				SELECT	u.user_id, u.user_name, r.role_id, r.role_name
				FROM 	#quoteIfNeeded("#arguments.prefix#users", arguments.dbtype)# u,
						#quoteIfNeeded("#arguments.prefix#roles", arguments.dbtype)# r
				WHERE	r.role_id = u.role_id
			');
		}
		/*
		query name="local.tables" params={ table: arguments.prefix & "%" } datasource=arguments.ds {
			echo("SELECT table_name
			FROM INFORMATION_SCHEMA.TABLES
			WHERE table_name LIKE :table ");
		}

		systemOutput("-- tables from INFORMATION_SCHEMA.TABLES ", true);
		loop query="tables"{
			systemOutput( arguments.dbType & " " & tables.table_name, true );
		}

		systemOutput("-- tables from dbinfo, filter='TABLE' ", true);
		dbinfo datasource=arguments.ds name="local.result" type="tables" pattern="#arguments.prefix#%" filter="TABLE";
		loop query="result"{
			systemOutput( arguments.dbType & " " & result.table_name, true );
		}

		systemOutput("-- tables from dbinfo, filter='table' ", true);
		dbinfo datasource=arguments.ds name="local.result" type="tables" pattern="#arguments.prefix#%" filter="table";
		loop query="result"{
			systemOutput( arguments.dbType & " " & result.table_name, true );
		}

		systemOutput("-- tables from dbinfo (no filter) ", true);
		dbinfo datasource=arguments.ds name="local.result" type="tables" pattern="#arguments.prefix#%";
		loop query="result"{
			systemOutput( arguments.dbType & " " & result.table_name, true );
		}
		*/

	}
}