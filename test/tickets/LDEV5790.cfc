component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll(){
		if (structKeyExists(server, "getDatasource")){
			// for testing via browser
			variables.h2 = server.getDatasource( "h2", server._getTempDir( "LDEV-5790" ) );
		} else {
			variables.tempDir = getTempDirectory() & createGUID();
			directoryCreate( variables.tempDir )
			variables.h2 =  {
				class: "org.h2.Driver",
				bundleName: "org.lucee.h2",
				bundleVersion: "2.1.214.0001L",
				connectionString: "jdbc:h2:#variables.tempDir#;MODE=MySQL"
			};
		}
	}

	function run( testResults , testBox ) {

		var dbs = [ "qoq", "h2" ];
		var clauseTypes = [ "varchar",  "bigint"]; // "float","integer" ];
		var colTypes = [ "varchar", "bigint" ]//, "double"] // "double", "integer" ];

		loop array=dbs item="db" {
			describe( title="LDEV-5790 #db# - check queries with and large numbers", body=function() {
				loop array=colTypes item="colType" {
					loop array=clauseTypes item="local.clauseType" {

						var testArgs = {
							db: db,
							colType: colType,
							clauseType: clauseType
						};

						it(title="col #colType# and with queryparam #clauseType#",
								data = testArgs,
								skip = checkSkip( testArgs ),
								body = function( data ) {
							testWith( data.db, data.colType, data.clauseType );
						});

						it(title="col #colType# without queryparam",
								data = testArgs ,
								skip = checkSkip( testArgs ),
								body = function( data ) {
							testWithout( data.db, data.colType );
						});
					};
				}
			});
		}

		describe( title="LDEV-5790 qoq - large numbers in other comparisons", body=function() {
			loop array=colTypes item="local.colType" {
				var testArgs = { colType: colType };

				it( title="col #colType# with an unquoted number literal", data=testArgs, body=function( data ) {
					var q = getQuery( data.colType );
					var res = queryExecute( "SELECT * from q where num = 996012564777658757", [], { dbtype="query" } );
					expect( res.recordCount ).toBe( 1 );
					expect( res.num[ 1 ] ).toBe( "996012564777658757" );
				});

				it( title="col #colType# with IN", data=testArgs, body=function( data ) {
					var q = getQuery( data.colType );
					var res = queryExecute( "SELECT * from q where num in ( '996012564777658758', '1' )", [], { dbtype="query" } );
					expect( res.recordCount ).toBe( 1 );
					expect( res.num[ 1 ] ).toBe( "996012564777658758" );
				});

				it( title="col #colType# with <> and >", data=testArgs, body=function( data ) {
					var q = getQuery( data.colType );
					var res = queryExecute( "SELECT * from q where num <> ?", [ { value: "996012564777658757", sqltype: "varchar" } ], { dbtype="query" } );
					expect( res.recordCount ).toBe( 1 );
					expect( res.num[ 1 ] ).toBe( "996012564777658758" );
					res = queryExecute( "SELECT * from q where num > ?", [ { value: "996012564777658757", sqltype: "bigint" } ], { dbtype="query" } );
					expect( res.recordCount ).toBe( 1 );
					expect( res.num[ 1 ] ).toBe( "996012564777658758" );
				});

				it( title="col #colType# with BETWEEN", data=testArgs, body=function( data ) {
					var q = getQuery( data.colType );
					var res = queryExecute( "SELECT * from q where num between 996012564777658758 and 996012564777658760", [], { dbtype="query" } );
					expect( res.recordCount ).toBe( 1 );
					expect( res.num[ 1 ] ).toBe( "996012564777658758" );
				});
			}
		});
	};

	private function checkSkip( args ){
		return false;
	}

	private function testWith ( db, colDataType, clauseDataType ){
		var q = getQuery( colDataType );
		expect( getMetadata( q )[ 1 ].typeName ) .toBe( arguments.colDataType ) ;
		var val = q.num[ 1 ];
		var q_with = testWithQueryParam( db, q, clauseDataType, val );

		//debug(arguments);
		//debug(q_with);
		//expect( q_with.r.sql ).toInclude( val );
		expect( q_with.q.recordCount ).toBe( 1 );
		expect( q_with.q.num[ 1 ] ).toBe( val );
		expect( q_with.r.sqlparameters[1] ).toBe( val );
	}

	private function testWithOut ( db, colDataType ){
		var q = getQuery( colDataType );
		expect( getMetadata( q )[ 1 ].typeName ) .toBe( arguments.colDataType ) ;
		var val = q.num[ 1 ];
		var q_without = testWithoutQueryParam( db, q, val );

		//debug(q_without);
		expect( q_without.q.recordCount ).toBe( 1 );
		expect( q_without.r.sql ).toInclude( val );
		expect( q_without.q.num[ 1 ] ).toBe( val );


	}

	private function testWithQueryParam( db, q, dataType, val ){
		var options = ( db == "qoq" ) ? { dbtype="query", result="r" } : { datasource=variables.h2, result="r" };
		if ( db != "qoq" )
			loadData( db, q );
		var qry = queryExecute(
			"SELECT * from q where num = ?",
			[ { value=val, sqltype=arguments.dataType } ],
			options
		);
		return {
			q: qry,
			r: r
		};
	}

	private function testWithoutQueryParam( db, q, val ){
		var options = ( db == "qoq" ) ? { dbtype="query", result="r" } : { datasource=variables.h2, result="r" };
		if ( db != "qoq" )
			loadData( db, q );
		var qry = queryExecute(
			"SELECT * from q where num = '#val#'",
			[],
			options
		);
		return {
			q: qry,
			r: r
		};
	}

	private function getQuery(colType){
		var q = queryNew( 'num', colType );
		queryAddRow( q );
		querySetCell(q, "num", '996012564777658757', 1);
		queryAddRow( q );
		querySetCell(q, "num", '996012564777658758', 2);
		/*
		queryAddRow( q );
		querySetCell(q, "num", '996012564777658759', 3);
		queryAddRow( q );
		querySetCell(q, "num", '996012564777658760', 4);
		queryAddRow( q );
		querySetCell(q, "num", '996012564777658761', 5);
		*/
		return q
	}

	private function loadData( db, q ){
		var dataType = getMetadata( q )[ 1 ].typeName;
		queryExecute(
			"DROP TABLE IF EXISTS q",
			[],
			{ datasource=variables.h2 }
		);
		queryExecute(
			"CREATE TABLE q ( num #dataType# )",
			[],
			{ datasource=variables.h2 }
		);
		loop query=q {
			queryExecute(
				"INSERT INTO q ( num ) VALUES ( ? )",
				[
					{ value: q.num, sqltype: dataType }
				],
				{ datasource=variables.h2 }
			);
		}
	}

}
