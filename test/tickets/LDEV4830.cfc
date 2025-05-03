component extends = "org.lucee.cfml.test.LuceeTestCase" labels="mssql"{

	function beforeAll(){
		if ( isNotSupported() )
			return;
		variables.ds = server.getDatasource("mssql");

		query datasource=ds {
			echo( "DROP TABLE IF EXISTS LDEV_4830" );
		}
		query datasource=ds {
			echo( "CREATE TABLE LDEV_4830( id int identity(1,1) primary key, name varchar(25))" );
		}
	}

	function afterAll(){
		if ( isNotSupported() )
			return;
		query datasource=ds{
			echo( "DROP TABLE IF EXISTS LDEV_4830" );
		}
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV-4830", function(){
			it( title = "Checking IDENTITYCOL is returned", skip=isNotSupported(), body = function( currentSpec ){

				_testInsert( ds, true );
				_testInsert( ds, false );
				var _ds = duplicate( ds );

				var jv = "jre8";

				// testing with jre8 as Lucee 6 still supports java 8
				_ds.bundleVersion = "12.4.2.#jv#";
				_testInsert( _ds, true);
				_testInsert( _ds, false);

				_ds.bundleVersion = "11.2.3.#jv#";
				_testInsert( _ds, true);
				_testInsert( _ds, false);

				_ds.bundleVersion = "9.4.1.#jv#";
				_testInsert( _ds, false);
				_testInsert( _ds, true);

			});
		});
	}

	private function _testInsert ( required struct _ds, required boolean useQueryExecute ){
		// test via explict datasource
		_doTestInsert(arguments._ds, arguments.useQueryExecute );
		// test via application datasource
		application action="update" datasource=_ds;
		_doTestInsert("", arguments.useQueryExecute );
	}

	private function _doTestInsert ( _ds, required boolean useQueryExecute ){
		var debug = false;

		if ( len( arguments._ds ) ) {
			dbinfo name="local.db_info" datasource=arguments._ds type="version";
			// if ( debug )
			// 	systemOutput( db_info, true );
		}
		// if ( debug )
		// 	systemOutput( "Using " & (arguments.useQueryExecute ? "queryExecute" : "cfquery"), true );

		var params = {
			name: "lucee"
		};
		if ( arguments.useQueryExecute ){
			// queryExecute is just a wrapper around cfquery
			var options = {
				result: "local.ins_result",
				datasource: arguments._ds
			};
			if ( !len( arguments._ds ) )
				structDelete( options, "datasource" );
			queryExecute( "INSERT INTO LDEV_4830 ( name ) values ( :name )",
				params,
				options
			);
		} else {
			if ( !len( arguments._ds ) ) {
				query params=params result="local.ins_result" {
					echo( "INSERT INTO LDEV_4830 ( name ) values ( :name )" );
				}
			} else {
				query datasource=arguments._ds params=params result="local.ins_result" {
					echo( "INSERT INTO LDEV_4830 ( name ) values ( :name )" );
				}
			}
		}
		// if ( debug )
		// 	systemOutput( ins_result, true );
		expect( ins_result.generatedKey ).toBe( 1 );
		expect( ins_result.identitycol ).toBe( 1 );

		if ( !len( arguments._ds ) ) {
			query result="local.result" name="local.qry" {
				echo( "SELECT id, name from LDEV_4830" );
			}
		} else {
			query datasource=arguments._ds result="local.result" name="local.qry" {
				echo( "SELECT id, name from LDEV_4830" );
			}
		}
		
		// if ( debug ) {
		// 	systemOutput( result, true );
		// 	systemOutput( qry, true );
		// }
		expect( result.recordcount ).toBe( 1 );
		expect( qry.recordcount ).toBe( 1 );
		expect( qry.id ).toBe( 1 );
		expect( qry.name ).toBe( "lucee" );

		if ( !len( arguments._ds ) ) {
			query {
				echo( "TRUNCATE TABLE LDEV_4830" );
			}
		} else {
			query datasource=arguments._ds {
				echo( "TRUNCATE TABLE LDEV_4830" );
			}
		}

	}

	private function isNotSupported() {
		var mssql = server.getDatasource("mssql");
		if(!isNull(mssql) && structCount(mssql)){
			return false;
		} else{
			return true;
		}
	}
}