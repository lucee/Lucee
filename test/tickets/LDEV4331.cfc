component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, testBox ) {

		describe( "LDEV-4331 QoQ native round( value [, numDigits=0 ] )", function() {

			// engine "native" never falls back to HSQLDB, so these only pass when QoQ handles round() itself
			it( title="rounds to a whole number by default", body=function( currentSpec ) {
				expect( nativeValue( "double", 1234.5678, "round( v )" ) ).toBe( 1235 );
				expect( nativeValue( "double", 1.4, "round( v )" ) ).toBe( 1 );
				expect( nativeValue( "double", 1.6, "round( v )" ) ).toBe( 2 );
				expect( nativeValue( "double", -1.6, "round( v )" ) ).toBe( -2 );
				expect( nativeValue( "double", 1234.5678, "round( v, 0 )" ) ).toBe( 1235 );
			});

			it( title="positive numDigits keeps that many decimals", body=function( currentSpec ) {
				expect( nativeValue( "double", 1234.5678, "round( v, 1 )" ) ).toBe( 1234.6 );
				expect( nativeValue( "double", 1234.5678, "round( v, 2 )" ) ).toBe( 1234.57 );
				expect( nativeValue( "double", 1234.5678, "round( v, 3 )" ) ).toBe( 1234.568 );
				expect( nativeValue( "double", 1234.5678, "round( v, 10 )" ) ).toBe( 1234.5678 );
				expect( nativeValue( "double", 15, "round( v, 2 )" ) ).toBe( 15 );
			});

			it( title="negative numDigits rounds left of the decimal point", body=function( currentSpec ) {
				expect( nativeValue( "double", 1234.5678, "round( v, -1 )" ) ).toBe( 1230 );
				expect( nativeValue( "double", 1234.5678, "round( v, -2 )" ) ).toBe( 1200 );
				expect( nativeValue( "double", 1234.5678, "round( v, -3 )" ) ).toBe( 1000 );
				expect( nativeValue( "double", 1234.5678, "round( v, -4 )" ) ).toBe( 0 );
				expect( nativeValue( "double", 15, "round( v, -1 )" ) ).toBe( 20 );
				expect( nativeValue( "double", -15, "round( v, -1 )" ) ).toBe( -20 );
			});

			it( title="DOUBLE values round half even, like HSQLDB", body=function( currentSpec ) {
				expect( nativeValue( "double", 2.5, "round( v )" ) ).toBe( 2 );
				expect( nativeValue( "double", -2.5, "round( v )" ) ).toBe( -2 );
				expect( nativeValue( "double", 3.5, "round( v )" ) ).toBe( 4 );
				expect( nativeValue( "double", 0.5, "round( v )" ) ).toBe( 0 );
				expect( nativeValue( "double", -0.5, "round( v )" ) ).toBe( 0 );
				expect( nativeValue( "double", 1.5, "round( v )" ) ).toBe( 2 );
				expect( nativeValue( "double", -1.5, "round( v )" ) ).toBe( -2 );
				expect( nativeValue( "double", 1.25, "round( v, 1 )" ) ).toBe( 1.2 );
				expect( nativeValue( "double", 1.35, "round( v, 1 )" ) ).toBe( 1.4 );
				expect( nativeValue( "double", 2.675, "round( v, 2 )" ) ).toBe( 2.68 );
				expect( nativeValue( "double", 1.005, "round( v, 2 )" ) ).toBe( 1 );
				expect( nativeValue( "double", 15, "round( v, -1 )" ) ).toBe( 20 );
				expect( nativeValue( "double", 25, "round( v, -1 )" ) ).toBe( 20 );
				expect( nativeValue( "double", -25, "round( v, -1 )" ) ).toBe( -20 );
			});

			it( title="DECIMAL values round half up, like HSQLDB", body=function( currentSpec ) {
				expect( nativeValue( "decimal", 2.5, "round( v )" ) ).toBe( 3 );
				expect( nativeValue( "decimal", -2.5, "round( v )" ) ).toBe( -3 );
				expect( nativeValue( "decimal", 0.5, "round( v )" ) ).toBe( 1 );
				expect( nativeValue( "decimal", -0.5, "round( v )" ) ).toBe( -1 );
				expect( nativeValue( "decimal", 1.25, "round( v, 1 )" ) ).toBe( 1.3 );
				expect( nativeValue( "decimal", 15, "round( v, -1 )" ) ).toBe( 20 );
				expect( nativeValue( "decimal", 25, "round( v, -1 )" ) ).toBe( 30 );
				expect( nativeValue( "decimal", -25, "round( v, -1 )" ) ).toBe( -30 );
			});

			it( title="INTEGER values round half up, like HSQLDB", body=function( currentSpec ) {
				expect( nativeValue( "integer", 15, "round( v, -1 )" ) ).toBe( 20 );
				expect( nativeValue( "integer", 25, "round( v, -1 )" ) ).toBe( 30 );
				expect( nativeValue( "integer", -25, "round( v, -1 )" ) ).toBe( -30 );
				expect( nativeValue( "integer", 24, "round( v, -1 )" ) ).toBe( 20 );
				expect( nativeValue( "integer", 25, "round( v )" ) ).toBe( 25 );
				expect( nativeValue( "integer", 25, "round( v, 2 )" ) ).toBe( 25 );
			});

			it( title="plain number literals round half up, like HSQLDB", body=function( currentSpec ) {
				expect( nativeLiteral( "round( 2.5 )" ) ).toBe( 3 );
				expect( nativeLiteral( "round( -2.5 )" ) ).toBe( -3 );
				expect( nativeLiteral( "round( 1.25, 1 )" ) ).toBe( 1.3 );
				expect( nativeLiteral( "round( -1.25, 1 )" ) ).toBe( -1.3 );
				expect( nativeLiteral( "round( 15, -1 )" ) ).toBe( 20 );
				expect( nativeLiteral( "round( 25, -1 )" ) ).toBe( 30 );
				expect( nativeLiteral( "round( -25, -1 )" ) ).toBe( -30 );
				expect( nativeLiteral( "round( 2.5, 0 )" ) ).toBe( 3 );
				// a number in exponent notation is a DOUBLE in HSQLDB
				expect( nativeLiteral( "round( 2.5e0 )" ) ).toBe( 2 );
			});

			it( title="null in gives null out", body=function( currentSpec ) {
				var qry = queryNew( "v,d", "double,integer", [ [ 1.5, 1 ] ] );
				querySetCell( qry, "v", javacast( "null", 0 ), 1 );
				var res = queryExecute( sql="SELECT round( v ) AS a, round( v, 1 ) AS b, round( v, d ) AS c FROM qry", options={ dbtype: { type: "query", engine: "native" } } );
				expect( res.recordCount ).toBe( 1 );
				expect( isNull( res.a[ 1 ] ) ).toBeTrue();
				expect( isNull( res.b[ 1 ] ) ).toBeTrue();
				expect( isNull( res.c[ 1 ] ) ).toBeTrue();

				var qry2 = queryNew( "v,d", "double,integer", [ [ 1.55, 1 ] ] );
				querySetCell( qry2, "d", javacast( "null", 0 ), 1 );
				res = queryExecute( sql="SELECT round( v, d ) AS a, round( v ) AS b FROM qry2", options={ dbtype: { type: "query", engine: "native" } } );
				expect( isNull( res.a[ 1 ] ) ).toBeTrue();
				expect( res.b[ 1 ] ).toBe( 2 );

				res = queryExecute( sql="SELECT round( NULL ) AS a, round( NULL, 1 ) AS b, round( 1.5, NULL ) AS c FROM qry2", options={ dbtype: { type: "query", engine: "native" } } );
				expect( isNull( res.a[ 1 ] ) ).toBeTrue();
				expect( isNull( res.b[ 1 ] ) ).toBeTrue();
				expect( isNull( res.c[ 1 ] ) ).toBeTrue();
			});

			it( title="works per row, in WHERE, ORDER BY and with aggregates", body=function( currentSpec ) {
				var qry = queryNew( "id,v", "integer,double", [ [ 1, 1.26 ], [ 2, 2.5 ], [ 3, 3.5 ], [ 4, 1.24 ] ] );
				var options = { dbtype: { type: "query", engine: "native" } };

				var res = queryExecute( sql="SELECT id, round( v, 1 ) AS r FROM qry ORDER BY id", options=options );
				expect( valueList( res.r ) ).toBe( "1.3,2.5,3.5,1.2" );

				res = queryExecute( sql="SELECT id FROM qry WHERE round( v ) = 2 ORDER BY id", options=options );
				expect( valueList( res.id ) ).toBe( "2" );

				res = queryExecute( sql="SELECT id FROM qry ORDER BY round( v, 1 ) DESC, id", options=options );
				expect( valueList( res.id ) ).toBe( "3,2,1,4" );

				res = queryExecute( sql="SELECT round( avg( v ), 2 ) AS a, sum( round( v ) ) AS s FROM qry", options=options );
				expect( res.a[ 1 ] ).toBe( 2.12 );
				expect( res.s[ 1 ] ).toBe( 8 );
			});

			it( title="native results are identical to the HSQLDB results", body=function( currentSpec ) {
				var values = [ 2.5, -2.5, 3.5, 1.5, -1.5, 0.5, -0.5, 0, 1.25, -1.25, 1.35, 2.675, -2.675, 1.005, -1.005, 15, -15, 25, -25, 35, 1234.5678, -1234.5678, 0.4999999999, 99.995, 1000000.5 ];
				var digits = [ 0, 1, 2, 3, -1, -2, -3 ];
				var mismatches = [];
				var compared = 0;
				for ( var type in [ "double", "decimal", "integer" ] ) {
					for ( var v in values ) {
						if ( type == "integer" && int( v ) != v ) continue;
						var qry = queryNew( "v", type, [ [ v ] ] );
						var selectList = "round( v ) AS r";
						for ( var d in digits ) selectList &= ", round( v, #d# ) AS r#replace( d, '-', 'm' )#";
						var nat = queryExecute( sql="SELECT " & selectList & " FROM qry", options={ dbtype: { type: "query", engine: "native" } } );
						var hsq = queryExecute( sql="SELECT " & selectList & " FROM qry", options={ dbtype: { type: "query", engine: "hsqldb" } } );
						for ( var col in listToArray( nat.columnList ) ) {
							compared++;
							if ( toString( nat[ col ][ 1 ] ) != toString( hsq[ col ][ 1 ] ) ) {
								arrayAppend( mismatches, "#type# #col# of #v#: native=#nat[ col ][ 1 ]# hsqldb=#hsq[ col ][ 1 ]#" );
							}
						}
					}
				}
				var literals = [ "round( 2.5 )", "round( -2.5 )", "round( 0.5 )", "round( 1.25, 1 )", "round( -1.25, 1 )", "round( 2.675, 2 )", "round( 1.005, 2 )", "round( 15, -1 )", "round( 25, -1 )", "round( -15, -1 )", "round( 1234.5678, -2 )", "round( 2.5e0 )", "round( 3.5e0 )", "round( 1.25e0, 1 )", "round( NULL )", "round( NULL, 1 )", "round( 1.5, NULL )" ];
				var qry = queryNew( "v", "double", [ [ 1 ] ] );
				for ( var lit in literals ) {
					var nat = queryExecute( sql="SELECT #lit# AS r FROM qry", options={ dbtype: { type: "query", engine: "native" } } );
					var hsq = queryExecute( sql="SELECT #lit# AS r FROM qry", options={ dbtype: { type: "query", engine: "hsqldb" } } );
					compared++;
					var n = isNull( nat.r[ 1 ] ) ? "<null>" : toString( nat.r[ 1 ] );
					var h = isNull( hsq.r[ 1 ] ) ? "<null>" : toString( hsq.r[ 1 ] );
					if ( n != h ) arrayAppend( mismatches, "#lit#: native=#n# hsqldb=#h#" );
				}
				expect( compared ).toBeGT( 300 );
				expect( mismatches ).toBe( [] );
			});

			it( title="the default engine returns the same as the HSQLDB engine", body=function( currentSpec ) {
				var qry = queryNew( "v", "double", [ [ 2.5 ], [ 1.25 ], [ 1234.5678 ] ] );
				var auto = queryExecute( sql="SELECT round( v ) AS a, round( v, 1 ) AS b, round( v, -2 ) AS c FROM qry", options={ dbtype: "query" } );
				var hsq = queryExecute( sql="SELECT round( v ) AS a, round( v, 1 ) AS b, round( v, -2 ) AS c FROM qry", options={ dbtype: { type: "query", engine: "hsqldb" } } );
				expect( valueList( auto.a ) ).toBe( valueList( hsq.a ) );
				expect( valueList( auto.b ) ).toBe( valueList( hsq.b ) );
				expect( valueList( auto.c ) ).toBe( valueList( hsq.c ) );
			});

			it( title="a numDigits with decimals is an error and is not swallowed", body=function( currentSpec ) {
				var qry = queryNew( "v", "double", [ [ 1.25 ] ] );
				expect( function() {
					queryExecute( sql="SELECT round( v, 1.5 ) AS r FROM qry", options={ dbtype: { type: "query", engine: "native" } } );
				}).toThrow();
			});

		});
	}

	private function nativeValue( required string type, required numeric value, required string expression ) {
		var qry = queryNew( "v", arguments.type, [ [ arguments.value ] ] );
		var res = queryExecute( sql="SELECT #arguments.expression# AS r FROM qry", options={ dbtype: { type: "query", engine: "native" } } );
		expect( res.recordCount ).toBe( 1 );
		return res.r[ 1 ];
	}

	private function nativeLiteral( required string expression ) {
		var qry = queryNew( "v", "double", [ [ 1 ] ] );
		var res = queryExecute( sql="SELECT #arguments.expression# AS r FROM qry", options={ dbtype: { type: "query", engine: "native" } } );
		expect( res.recordCount ).toBe( 1 );
		return res.r[ 1 ];
	}

}
