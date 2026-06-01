component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" skip="true" {

	variables.tableName    = "ldev6304_" & left( lcase( hash( createUniqueID() ) ), 12 );
	variables.testValues   = [ 2.01, 23.44, 17.8, 0.00000001 ];
	variables._datasources = configureDatasources();

	function afterAll() {
		loop collection=variables._datasources key="local.dbType" value="local.ds" {
			dropTable( ds=local.ds, dbType=local.dbType );
		}
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6304 cfqueryparam float round-trip — JDBC spec compliance epic", function() {

			loop collection=variables._datasources key="local.dbType" value="local.ds" {

				createTable( ds=local.ds, dbType=local.dbType );

				describe( title="round-trip on [#local.dbType#]", body=function() {

					it(
						title = "dbinfo reports double/single/decimal columns [#dbType#]",
						data  = { ds: ds, dbType: dbType },
						body  = function( data ) {
							assertColumnTypes( ds=data.ds, dbType=data.dbType );
						}
					);

					it(
						title = "LDEV-6305 control: CF_SQL_DOUBLE against double-precision column [#dbType#]",
						data  = { ds: ds, dbType: dbType },
						body  = function( data ) {
							assertRoundTripAll( ds=data.ds, dbType=data.dbType, columnName="val_dbl", cfsqltype="CF_SQL_DOUBLE" );
						}
					);

					it(
						title = "LDEV-6305 control: CF_SQL_REAL against single-precision column [#dbType#]",
						data  = { ds: ds, dbType: dbType },
						body  = function( data ) {
							assertRoundTripAll( ds=data.ds, dbType=data.dbType, columnName="val_sgl", cfsqltype="CF_SQL_REAL" );
						}
					);

					it(
						title = "LDEV-6305 workaround: CF_SQL_NUMERIC against double-precision column [#dbType#]",
						data  = { ds: ds, dbType: dbType },
						body  = function( data ) {
							assertRoundTripAll( ds=data.ds, dbType=data.dbType, columnName="val_dbl", cfsqltype="CF_SQL_NUMERIC" );
						}
					);

					it(
						title = "LDEV-6305 workaround: CF_SQL_DECIMAL against double-precision column [#dbType#]",
						data  = { ds: ds, dbType: dbType },
						body  = function( data ) {
							assertRoundTripAll( ds=data.ds, dbType=data.dbType, columnName="val_dbl", cfsqltype="CF_SQL_DECIMAL" );
						}
					);

					it(
						title = "LDEV-6305 fix: CF_SQL_FLOAT against double-precision column [#dbType#]",
						data  = { ds: ds, dbType: dbType },
						body  = function( data ) {
							assertRoundTripAll( ds=data.ds, dbType=data.dbType, columnName="val_dbl", cfsqltype="CF_SQL_FLOAT" );
						}
					);

					it(
						title = "LDEV-3022 fix: CF_SQL_FLOAT against DECIMAL column [#dbType#]",
						data  = { ds: ds, dbType: dbType },
						body  = function( data ) {
							assertRoundTripAll( ds=data.ds, dbType=data.dbType, columnName="val_dec", cfsqltype="CF_SQL_FLOAT" );
						}
					);

					it(
						title = "LDEV-3022 workaround: CF_SQL_DECIMAL against DECIMAL column [#dbType#]",
						data  = { ds: ds, dbType: dbType },
						body  = function( data ) {
							assertRoundTripAll( ds=data.ds, dbType=data.dbType, columnName="val_dec", cfsqltype="CF_SQL_DECIMAL" );
						}
					);

					it(
						title = "LDEV-2423 fix: literal 1E-8 = CF_SQL_FLOAT param 0.00000001 [#dbType#]",
						data  = { ds: ds, dbType: dbType },
						body  = function( data ) {
							expect( jdbcLiteralEqualsParam( ds=data.ds, literal="1E-8", cfsqltype="CF_SQL_FLOAT", value=0.00000001 ) ).toBeTrue();
						}
					);

					it(
						title = "LDEV-2423 workaround: literal 1E-8 = CF_SQL_NUMERIC param 0.00000001 [#dbType#]",
						data  = { ds: ds, dbType: dbType },
						body  = function( data ) {
							expect( jdbcLiteralEqualsParam( ds=data.ds, literal="1E-8", cfsqltype="CF_SQL_NUMERIC", value=0.00000001 ) ).toBeTrue();
						}
					);

				});
			}

			describe( "QoQ — same param-binding plumbing without a JDBC datasource", function() {

				it( title="LDEV-2423: literal 1E-8 round-trips through QoQ projection", body=function() {
					var q = queryNew( "id" );
					queryAddRow( q );
					var res = queryExecute( "SELECT 1E-8 AS num FROM q", {}, { dbtype="query" } );
					expect( res.num ).toBe( 1E-8 );
				});

				it( title="LDEV-2423 control: CF_SQL_DOUBLE param compares equal to literal 1E-8", body=function() {
					expect( qoqLiteralEqualsParam( literal=1E-8, cfsqltype="CF_SQL_DOUBLE", value=0.00000001 ) ).toBe( 1 );
				});

				it( title="LDEV-2423 workaround: CF_SQL_NUMERIC param compares equal to literal 1E-8", body=function() {
					expect( qoqLiteralEqualsParam( literal=1E-8, cfsqltype="CF_SQL_NUMERIC", value=0.00000001 ) ).toBe( 1 );
				});

				it( title="LDEV-2423 fix: CF_SQL_FLOAT param compares equal to literal 1E-8", body=function() {
					expect( qoqLiteralEqualsParam( literal=1E-8, cfsqltype="CF_SQL_FLOAT", value=0.00000001 ) ).toBe( 1 );
				});

			});

		});
	}

	// --- private helpers ---

	private struct function configureDatasources() {
		var datasources = [
			mysql:    server.getDatasource( "mysql"    ),
			oracle:   server.getDatasource( "oracle"   ),
			mssql:    server.getDatasource( "mssql"    ),
			postgres: server.getDatasource( "postgres" ),
			h2:       server.getDatasource( "h2",     server._getTempDir( "ldev6304-h2"     ) ),
			hsqldb:   server.getDatasource( "hsqldb", server._getTempDir( "ldev6304-hsqldb" ) )
		];
		return structFilter( datasources, function( k, v ) {
			return !isEmpty( arguments.v );
		});
	}

	// JDBC spec: Types.FLOAT == double precision, Types.REAL == single precision.
	// MySQL deviates — its FLOAT is single, DOUBLE is double. Oracle uses BINARY_DOUBLE/FLOAT.
	private string function columnDDL( dbType ) {
		switch ( arguments.dbType ) {
			case "mysql":    return "val_dbl DOUBLE,           val_sgl FLOAT,        val_dec DECIMAL(20,10)";
			case "postgres": return "val_dbl DOUBLE PRECISION, val_sgl REAL,         val_dec NUMERIC(20,10)";
			case "oracle":   return "val_dbl BINARY_DOUBLE,    val_sgl BINARY_FLOAT, val_dec NUMBER(20,10)";
			default:         return "val_dbl FLOAT,            val_sgl REAL,         val_dec DECIMAL(20,10)";
		}
	}

	private void function createTable( ds, dbType ) {
		dropTable( ds=arguments.ds, dbType=arguments.dbType );
		queryExecute( "CREATE TABLE #variables.tableName# ( #columnDDL( arguments.dbType )# )", {}, { datasource=arguments.ds } );

		for ( var v in variables.testValues ) {
			queryExecute(
				"INSERT INTO #variables.tableName# ( val_dbl, val_sgl, val_dec ) VALUES ( :d, :s, :n )",
				{
					d = { value=v, cfsqltype="CF_SQL_DOUBLE"  },
					s = { value=v, cfsqltype="CF_SQL_REAL"    },
					n = { value=v, cfsqltype="CF_SQL_DECIMAL" }
				},
				{ datasource=arguments.ds }
			);
		}
	}

	private void function dropTable( ds, dbType ) {
		try {
			if ( arguments.dbType == "oracle" ) {
				queryExecute( "DROP TABLE #variables.tableName#", {}, { datasource=arguments.ds } );
			} else if ( arguments.dbType == "mssql" ) {
				queryExecute( "IF OBJECT_ID( '#variables.tableName#', 'U' ) IS NOT NULL DROP TABLE #variables.tableName#", {}, { datasource=arguments.ds } );
			} else {
				queryExecute( "DROP TABLE IF EXISTS #variables.tableName#", {}, { datasource=arguments.ds } );
			}
		} catch ( any e ) {
			// table may not exist on first run
		}
	}

	private numeric function runRoundTrip( ds, columnName, cfsqltype, value ) {
		var qry = queryExecute(
			"SELECT 1 AS hit FROM #variables.tableName# WHERE #arguments.columnName# = :p",
			{ p = { cfsqltype=arguments.cfsqltype, value=arguments.value } },
			{ datasource=arguments.ds }
		);
		return qry.recordCount;
	}

	private void function assertRoundTripAll( ds, dbType, columnName, cfsqltype ) {
		// MySQL widens FLOAT cols to DOUBLE for comparison (Bug #87794 "Not a Bug" + Manual B.3.4.8).
		// Single-precision 2.01 widened ≠ double-precision 2.01, so any equality WHERE against a
		// MySQL FLOAT col is unreliable by design. MySQL itself recommends DOUBLE/DECIMAL or tolerance.
		if ( arguments.dbType == "mysql" && arguments.cfsqltype == "CF_SQL_REAL" ) return;

		for ( var v in variables.testValues ) {
			var rows = runRoundTrip( ds=arguments.ds, columnName=arguments.columnName, cfsqltype=arguments.cfsqltype, value=v );
			expect( rows ).toBe( 1, "[#arguments.dbType#] value #v# via #arguments.cfsqltype# against #arguments.columnName# returned #rows# rows" );
		}
	}

	private boolean function jdbcLiteralEqualsParam( ds, literal, cfsqltype, value ) {
		var qry = queryExecute(
			"SELECT 1 FROM #variables.tableName# WHERE #arguments.literal# = :p",
			{ p = { cfsqltype=arguments.cfsqltype, value=arguments.value } },
			{ datasource=arguments.ds }
		);
		return qry.recordCount > 0;
	}

	private void function assertColumnTypes( ds, dbType ) {
		dbinfo type="columns" datasource=arguments.ds table=variables.tableName name="local.cols";
		var byName = {};
		loop query="local.cols" {
			byName[ lcase( cols.column_name ) ] = lcase( cols.type_name );
		}
		expect( byName ).toHaveKey( "val_dbl", "[#arguments.dbType#] dbinfo missing val_dbl" );
		expect( byName ).toHaveKey( "val_sgl", "[#arguments.dbType#] dbinfo missing val_sgl" );
		expect( byName ).toHaveKey( "val_dec", "[#arguments.dbType#] dbinfo missing val_dec" );
	}

	private numeric function qoqLiteralEqualsParam( literal, cfsqltype, value ) {
		var q = queryNew( "id" );
		queryAddRow( q );
		var res = queryExecute(
			"SELECT 1 FROM q WHERE #arguments.literal# = :p",
			{ p = { cfsqltype=arguments.cfsqltype, value=arguments.value } },
			{ dbtype="query" }
		);
		return res.recordCount;
	}

}
