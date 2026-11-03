component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function afterAll() {
		application action="update" nullSupport=false;
	}

	// returns the concrete java class name of a value, e.g. "java.lang.Double".
	// we check the real runtime type on purpose: isNumeric()/isBoolean()/isDate()
	// only test whether a value *could* be converted, not what it actually is.
	private string function typeOf( any value ) {
		return arguments.value.getClass().getName();
	}

	private query function typedQuery() {
		return queryNew(
			"int,small,tiny,big,dbl,flt,rl,num,dec,dt,tm,ts,vc,ch,nvc,bt,bl",
			"integer,smallint,tinyint,bigint,double,float,real,numeric,decimal,date,time,timestamp,varchar,char,nvarchar,bit,boolean",
			[
				[ "1", "2", "3", "4", "5.5", "6.5", "7.5", "8.5", "9.25", "2026-01-15", "12:30:15", "2026-08-28 10:11:12", 12, "c", "n", "1", "true" ],
				[ "10", "20", "30", "40", "50.5", "60.5", "70.5", "80.5", "90.25", "2026-12-31", "01:02:03", "2026-12-31 23:59:59", "text", "d", "m", "0", "false" ]
			]
		);
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6454 queryConvertTypes()", function() {

			afterEach( function( currentSpec, data ) {
				application action="update" nullSupport=false;
			});

			it( title="converts the values of each column type to the java type of the column", body=function( currentSpec ) {
				var res = queryConvertTypes( typedQuery() );

				expect( typeOf( res.int[ 1 ] ) ).toBe( "java.lang.Integer" );
				expect( typeOf( res.small[ 1 ] ) ).toBe( "java.lang.Integer" );
				expect( typeOf( res.tiny[ 1 ] ) ).toBe( "java.lang.Integer" );
				expect( typeOf( res.big[ 1 ] ) ).toBe( "java.lang.Long" );
				expect( typeOf( res.dbl[ 1 ] ) ).toBe( "java.lang.Double" );
				expect( typeOf( res.flt[ 1 ] ) ).toBe( "java.lang.Double" );
				expect( typeOf( res.rl[ 1 ] ) ).toBe( "java.lang.Double" );
				expect( typeOf( res.num[ 1 ] ) ).toBe( "java.lang.Double" );
				expect( typeOf( res.dec[ 1 ] ) ).toBe( "java.math.BigDecimal" );
				expect( typeOf( res.dt[ 1 ] ) ).toBe( "lucee.runtime.type.dt.DateTimeImpl" );
				expect( typeOf( res.tm[ 1 ] ) ).toBe( "lucee.runtime.type.dt.DateTimeImpl" );
				expect( typeOf( res.ts[ 1 ] ) ).toBe( "lucee.runtime.type.dt.DateTimeImpl" );
				expect( typeOf( res.vc[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( res.ch[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( res.nvc[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( res.bt[ 1 ] ) ).toBe( "java.lang.Boolean" );
				expect( typeOf( res.bl[ 1 ] ) ).toBe( "java.lang.Boolean" );

				// all rows are converted, not only the first
				expect( typeOf( res.int[ 2 ] ) ).toBe( "java.lang.Integer" );
				expect( typeOf( res.big[ 2 ] ) ).toBe( "java.lang.Long" );
				expect( typeOf( res.dec[ 2 ] ) ).toBe( "java.math.BigDecimal" );
				expect( typeOf( res.ts[ 2 ] ) ).toBe( "lucee.runtime.type.dt.DateTimeImpl" );
				expect( typeOf( res.bt[ 2 ] ) ).toBe( "java.lang.Boolean" );
			});

			it( title="keeps the values correct after the conversion", body=function( currentSpec ) {
				var res = queryConvertTypes( typedQuery() );

				expect( res.int[ 1 ] ).toBe( 1 );
				expect( res.small[ 2 ] ).toBe( 20 );
				expect( res.tiny[ 2 ] ).toBe( 30 );
				expect( res.big[ 2 ] ).toBe( 40 );
				expect( res.dbl[ 1 ] ).toBe( 5.5 );
				expect( res.flt[ 1 ] ).toBe( 6.5 );
				expect( res.rl[ 1 ] ).toBe( 7.5 );
				expect( res.num[ 1 ] ).toBe( 8.5 );
				expect( res.dec[ 1 ].toString() ).toBe( "9.25" );
				expect( year( res.dt[ 1 ] ) ).toBe( 2026 );
				expect( month( res.dt[ 1 ] ) ).toBe( 1 );
				expect( day( res.dt[ 1 ] ) ).toBe( 15 );
				expect( hour( res.tm[ 1 ] ) ).toBe( 12 );
				expect( minute( res.tm[ 1 ] ) ).toBe( 30 );
				expect( second( res.tm[ 1 ] ) ).toBe( 15 );
				expect( second( res.ts[ 2 ] ) ).toBe( 59 );
				expect( res.vc[ 1 ] ).toBe( "12" );
				expect( res.vc[ 2 ] ).toBe( "text" );
				expect( res.ch[ 1 ] ).toBe( "c" );
				expect( res.bt[ 1 ] ).toBeTrue();
				expect( res.bt[ 2 ] ).toBeFalse();
				expect( res.bl[ 1 ] ).toBeTrue();
				expect( res.bl[ 2 ] ).toBeFalse();
				expect( res.recordcount ).toBe( 2 );
				expect( res.columnlist ).toBe( typedQuery().columnlist );
			});

			it( title="keeps the declared column types", body=function( currentSpec ) {
				var qry = typedQuery();
				var res = queryConvertTypes( qry );
				var before = getMetaData( qry );
				var after = getMetaData( res );
				expect( arrayLen( after ) ).toBe( arrayLen( before ) );
				loop from=1 to=arrayLen( before ) index="local.i" {
					expect( after[ i ].name ).toBe( before[ i ].name );
					expect( after[ i ].typeName ).toBe( before[ i ].typeName );
				}
			});

			it( title="converts values that are already of another java type", body=function( currentSpec ) {
				var qry = queryNew( "n,l,d,s,b", "integer,bigint,double,varchar,boolean", [
					[ 12.0, 5, 3, now(), 1 ]
				] );
				var res = queryConvertTypes( qry );
				expect( typeOf( res.n[ 1 ] ) ).toBe( "java.lang.Integer" );
				expect( typeOf( res.l[ 1 ] ) ).toBe( "java.lang.Long" );
				expect( typeOf( res.d[ 1 ] ) ).toBe( "java.lang.Double" );
				expect( typeOf( res.s[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( res.b[ 1 ] ) ).toBe( "java.lang.Boolean" );
			});

			it( title="returns a converted copy and leaves the original untouched by default", body=function( currentSpec ) {
				var qry = typedQuery();
				var res = queryConvertTypes( qry );

				expect( isQuery( res ) ).toBeTrue();
				expect( typeOf( qry.int[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( qry.big[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( qry.dec[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( qry.dt[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( qry.bt[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( res.int[ 1 ] ) ).toBe( "java.lang.Integer" );

				// explicit inline=false is the same
				var res2 = queryConvertTypes( query=qry, inline=false );
				expect( typeOf( qry.int[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( res2.int[ 1 ] ) ).toBe( "java.lang.Integer" );

				// changing the copy does not change the original
				querySetCell( res, "vc", "changed", 1 );
				expect( qry.vc[ 1 ] ).toBe( "12" );
			});

			it( title="inline=true modifies the given query and returns it", body=function( currentSpec ) {
				var qry = typedQuery();
				var res = queryConvertTypes( qry, true );

				expect( typeOf( qry.int[ 1 ] ) ).toBe( "java.lang.Integer" );
				expect( typeOf( qry.big[ 2 ] ) ).toBe( "java.lang.Long" );
				expect( typeOf( qry.dec[ 1 ] ) ).toBe( "java.math.BigDecimal" );
				expect( typeOf( qry.dt[ 1 ] ) ).toBe( "lucee.runtime.type.dt.DateTimeImpl" );
				expect( typeOf( qry.bt[ 1 ] ) ).toBe( "java.lang.Boolean" );

				// the same query is returned, not a copy
				querySetCell( res, "vc", "changed", 1 );
				expect( qry.vc[ 1 ] ).toBe( "changed" );
			});

			it( title="is available as member function", body=function( currentSpec ) {
				var qry = typedQuery();
				var res = qry.convertTypes();
				expect( typeOf( res.int[ 1 ] ) ).toBe( "java.lang.Integer" );
				expect( typeOf( res.dec[ 1 ] ) ).toBe( "java.math.BigDecimal" );
				expect( typeOf( qry.int[ 1 ] ) ).toBe( "java.lang.String" );

				res = qry.convertTypes( true );
				expect( typeOf( qry.int[ 1 ] ) ).toBe( "java.lang.Integer" );
				expect( typeOf( qry.dt[ 1 ] ) ).toBe( "lucee.runtime.type.dt.DateTimeImpl" );
			});

			it( title="leaves null as null", body=function( currentSpec ) {
				var qry = queryNew( "n,d,s,b", "integer,date,varchar,boolean" );
				queryAddRow( qry, 2 );
				querySetCell( qry, "n", "7", 1 );
				querySetCell( qry, "d", "2026-01-01", 1 );
				querySetCell( qry, "s", "x", 1 );
				querySetCell( qry, "b", "true", 1 );
				// row 2 stays empty (null)

				application action="update" nullSupport=true;
				var res = queryConvertTypes( qry );

				expect( typeOf( res.n[ 1 ] ) ).toBe( "java.lang.Integer" );
				expect( isNull( res.n[ 2 ] ) ).toBeTrue();
				expect( isNull( res.d[ 2 ] ) ).toBeTrue();
				expect( isNull( res.s[ 2 ] ) ).toBeTrue();
				expect( isNull( res.b[ 2 ] ) ).toBeTrue();
			});

			it( title="turns an empty string of a non string column into null with full null support", body=function( currentSpec ) {
				var qry = queryNew( "n,l,dbl,dec,d,b,s", "integer,bigint,double,decimal,timestamp,boolean,varchar", [
					[ "", "", "", "", "", "", "" ],
					[ "1", "2", "3", "4", "2026-01-01", "true", "text" ]
				] );

				application action="update" nullSupport=true;
				var res = queryConvertTypes( qry );

				expect( isNull( res.n[ 1 ] ) ).toBeTrue();
				expect( isNull( res.l[ 1 ] ) ).toBeTrue();
				expect( isNull( res.dbl[ 1 ] ) ).toBeTrue();
				expect( isNull( res.dec[ 1 ] ) ).toBeTrue();
				expect( isNull( res.d[ 1 ] ) ).toBeTrue();
				expect( isNull( res.b[ 1 ] ) ).toBeTrue();

				// a string column keeps the empty string
				expect( isNull( res.s[ 1 ] ) ).toBeFalse();
				expect( res.s[ 1 ] ).toBe( "" );

				// the other row is converted as usual
				expect( typeOf( res.n[ 2 ] ) ).toBe( "java.lang.Integer" );
				expect( typeOf( res.d[ 2 ] ) ).toBe( "lucee.runtime.type.dt.DateTimeImpl" );
			});

			it( title="keeps an empty string as it is without full null support, like the query itself accepts it", body=function( currentSpec ) {
				var qry = queryNew( "n,d,b,s", "integer,timestamp,boolean,varchar", [
					[ "", "", "", "" ],
					[ "1", "2026-01-01", "true", "text" ]
				] );

				application action="update" nullSupport=false;
				var res = queryConvertTypes( qry );

				expect( res.n[ 1 ] ).toBe( "" );
				expect( res.d[ 1 ] ).toBe( "" );
				expect( res.b[ 1 ] ).toBe( "" );
				expect( res.s[ 1 ] ).toBe( "" );
				expect( typeOf( res.n[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( res.n[ 2 ] ) ).toBe( "java.lang.Integer" );
				expect( typeOf( res.d[ 2 ] ) ).toBe( "lucee.runtime.type.dt.DateTimeImpl" );
				expect( typeOf( res.b[ 2 ] ) ).toBe( "java.lang.Boolean" );
			});

			it( title="leaves OTHER and OBJECT columns untouched, including complex values", body=function( currentSpec ) {
				var qry = queryNew( "o,x,n", "object,other,integer" );
				queryAddRow( qry, 3 );
				var arr = [ 1, 2, 3 ];
				querySetCell( qry, "o", { foo: "bar" }, 1 );
				querySetCell( qry, "o", arr, 2 );
				querySetCell( qry, "o", queryNew( "x", "integer", [ [ 42 ] ] ), 3 );
				// a struct as first value keeps the type of an untyped column at OTHER
				querySetCell( qry, "x", { a: 1 }, 1 );
				querySetCell( qry, "x", "2026-01-01", 2 );
				querySetCell( qry, "x", true, 3 );
				querySetCell( qry, "n", "1", 1 );
				querySetCell( qry, "n", "2", 2 );
				querySetCell( qry, "n", "3", 3 );

				var res = queryConvertTypes( qry );

				expect( isStruct( res.o[ 1 ] ) ).toBeTrue();
				expect( res.o[ 1 ].foo ).toBe( "bar" );
				expect( isArray( res.o[ 2 ] ) ).toBeTrue();
				expect( arrayLen( res.o[ 2 ] ) ).toBe( 3 );
				expect( isQuery( res.o[ 3 ] ) ).toBeTrue();
				expect( res.o[ 3 ].x[ 1 ] ).toBe( 42 );

				// untyped values keep their type
				expect( isStruct( res.x[ 1 ] ) ).toBeTrue();
				expect( typeOf( res.x[ 2 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( res.x[ 3 ] ) ).toBe( typeOf( qry.x[ 3 ] ) );

				// the typed column next to them is converted
				expect( typeOf( res.n[ 1 ] ) ).toBe( "java.lang.Integer" );

				// same with inline
				queryConvertTypes( qry, true );
				expect( isStruct( qry.o[ 1 ] ) ).toBeTrue();
				expect( isStruct( qry.x[ 1 ] ) ).toBeTrue();
				expect( typeOf( qry.x[ 2 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( qry.n[ 1 ] ) ).toBe( "java.lang.Integer" );
			});

			it( title="works with an empty query and a query without typed values", body=function( currentSpec ) {
				var res = queryConvertTypes( queryNew( "a,b", "integer,varchar" ) );
				expect( res.recordcount ).toBe( 0 );
				expect( res.columnlist ).toBe( "a,b" );

				res = queryConvertTypes( queryNew( "a,b", "integer,varchar" ), true );
				expect( res.recordcount ).toBe( 0 );
			});

			it( title="throws an exception naming column and row for a value that cannot be converted to the column type without losing data", body=function( currentSpec ) {
				var qry = queryNew( "id,amount", "integer,integer", [
					[ 1, 1 ],
					[ 2, "2.5" ]
				] );
				var msg = "";
				try {
					queryConvertTypes( qry );
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toInclude( "amount" );
				expect( msg ).toInclude( "row [2]" );
				expect( msg ).toInclude( "2.5" );
				expect( msg ).toInclude( "INTEGER" );
			});

			it( title="throws for every integral type when a value does not fit", body=function( currentSpec ) {
				var cases = [
					{ type: "integer", value: "1.5" },
					{ type: "integer", value: "3000000000" },
					{ type: "integer", value: 1.5 },
					{ type: "tinyint", value: "1.5" },
					{ type: "bigint", value: "1.5" },
					{ type: "bigint", value: "99999999999999999999" }
				];
				loop array=cases item="local.c" {
					var qry = queryNew( "col", c.type );
					queryAddRow( qry );
					querySetCell( qry, "col", c.value, 1 );
					expect( function() {
						queryConvertTypes( qry );
					}).toThrow( regex="column \[col\], row \[1\].*\[#uCase( c.type )#\]" );
				}
			});

			it( title="leaves a column untouched whose type was already reset to OTHER by values that do not fit its declared type", body=function( currentSpec ) {
				// the query itself resets the type of a column to OTHER as soon as it gets a value that cannot be cast
				// to the declared type, such a column is not typed anymore and is not converted
				var cases = [
					{ type: "integer", value: "abc", good: "1" },
					{ type: "double", value: "abc", good: "1" },
					{ type: "decimal", value: "abc", good: "1" },
					{ type: "timestamp", value: "not a date", good: "2026-01-01" },
					{ type: "boolean", value: "maybe", good: "true" },
					{ type: "integer", value: [ 1, 2 ], good: "1" },
					{ type: "varchar", value: { a: 1 }, good: "x" }
				];
				loop array=cases item="local.c" {
					var qry = queryNew( "col", c.type );
					queryAddRow( qry, 2 );
					querySetCell( qry, "col", c.good, 1 );
					querySetCell( qry, "col", c.value, 2 );
					var res = queryConvertTypes( qry );
					expect( typeOf( res.col[ 1 ] ) ).toBe( "java.lang.String" );
					expect( typeOf( res.col[ 2 ] ) ).toBe( typeOf( c.value ) );
				}
			});

			it( title="throws when a number does not fit the integer type without losing data", body=function( currentSpec ) {
				var qry = queryNew( "n", "integer", [ [ "1.5" ] ] );
				expect( function() {
					queryConvertTypes( qry );
				}).toThrow( regex="column \[n\], row \[1\]" );

				qry = queryNew( "n", "integer", [ [ "3000000000" ] ] );
				expect( function() {
					queryConvertTypes( qry );
				}).toThrow( regex="column \[n\], row \[1\]" );

				qry = queryNew( "n", "bigint", [ [ "1.5" ] ] );
				expect( function() {
					queryConvertTypes( qry );
				}).toThrow( regex="column \[n\], row \[1\]" );

				// but a whole number written as decimal is fine, and a long fits into a bigint
				qry = queryNew( "i,l", "integer,bigint", [ [ "3.0", "3000000000" ] ] );
				var res = queryConvertTypes( qry );
				expect( typeOf( res.i[ 1 ] ) ).toBe( "java.lang.Integer" );
				expect( res.i[ 1 ] ).toBe( 3 );
				expect( typeOf( res.l[ 1 ] ) ).toBe( "java.lang.Long" );
				expect( res.l[ 1 ] ).toBe( 3000000000 );
			});

			it( title="does not modify the query with inline=true when a value cannot be converted", body=function( currentSpec ) {
				var qry = queryNew( "a,b", "integer,integer", [
					[ "1", "2" ],
					[ "3", "4.5" ]
				] );
				expect( function() {
					queryConvertTypes( qry, true );
				}).toThrow( regex="column \[b\], row \[2\]" );

				// nothing is converted, also not the valid values before the failing one
				expect( typeOf( qry.a[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( qry.b[ 1 ] ) ).toBe( "java.lang.String" );
				expect( typeOf( qry.a[ 2 ] ) ).toBe( "java.lang.String" );
			});

			it( title="throws for the member function as well", body=function( currentSpec ) {
				var qry = queryNew( "n", "integer", [ [ "1.5" ] ] );
				expect( function() {
					qry.convertTypes();
				}).toThrow( regex="column \[n\], row \[1\]" );
			});

			it( title="throws for an argument that is not a query", body=function( currentSpec ) {
				expect( function() {
					queryConvertTypes( "abc" );
				}).toThrow();
			});

		});
	}

}
