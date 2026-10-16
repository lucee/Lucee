component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll() {
		variables.nativeQoQ = { dbtype: { type: "query", engine: "native" } };
		variables.hsqldbQoQ = { dbtype: { type: "query", engine: "hsqldb" } };
	}

	function run( testResults, testBox ) {

		describe( "LDEV-4183 QoQ || concat operator - native engine", function() {

			it( title="basic string || string", body=function() {
				basicConcat( variables.nativeQoQ );
			});

			it( title="column || string literal", body=function() {
				columnConcatLiteral( variables.nativeQoQ );
			});

			it( title="string || column || string (chained)", body=function() {
				chainedConcat( variables.nativeQoQ );
			});

			it( title="|| with NULL column", body=function() {
				concatWithNull( variables.nativeQoQ );
			});

			it( title="|| with empty string column", body=function() {
				concatWithEmpty( variables.nativeQoQ );
			});

			it( title="|| in WHERE clause", body=function() {
				concatInWhere( variables.nativeQoQ );
			});

			it( title="|| in ORDER BY", body=function() {
				concatInOrderBy( variables.nativeQoQ );
			});

			it( title="|| with numeric column coerces to string", body=function() {
				concatNumeric( variables.nativeQoQ );
			});

			it( title="|| mixed with + arithmetic", body=function() {
				concatMixedWithPlus( variables.nativeQoQ );
			});

			it( title="|| with multiple columns", body=function() {
				concatMultipleColumns( variables.nativeQoQ );
			});

		});

		describe( "LDEV-4183 QoQ || concat operator - HSQLDB engine", function() {

			it( title="basic string || string", body=function() {
				basicConcat( variables.hsqldbQoQ );
			});

			it( title="column || string literal", body=function() {
				columnConcatLiteral( variables.hsqldbQoQ );
			});

			it( title="string || column || string (chained)", body=function() {
				chainedConcat( variables.hsqldbQoQ );
			});

			it( title="|| with NULL column", body=function() {
				concatWithNull( variables.hsqldbQoQ );
			});

			it( title="|| with empty string column", body=function() {
				concatWithEmpty( variables.hsqldbQoQ );
			});

			it( title="|| in WHERE clause", body=function() {
				concatInWhere( variables.hsqldbQoQ );
			});

			it( title="|| in ORDER BY", body=function() {
				concatInOrderBy( variables.hsqldbQoQ );
			});

			it( title="|| with numeric column coerces to string", body=function() {
				concatNumeric( variables.hsqldbQoQ );
			});

			it( title="|| mixed with + arithmetic", body=function() {
				concatMixedWithPlus( variables.hsqldbQoQ );
			});

			it( title="|| with multiple columns", body=function() {
				concatMultipleColumns( variables.hsqldbQoQ );
			});

		});

	}

	private function basicConcat( opts ) {
		var qry = queryNew( "id", "integer", [ [ 1 ] ] );
		var result = queryExecute(
			"SELECT 'foo' || 'bar' AS val FROM qry",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.val ).toBe( "foobar" );
	}

	private function columnConcatLiteral( opts ) {
		var qry = queryNew( "name", "varchar", [ [ "alice" ], [ "bob" ] ] );
		var result = queryExecute(
			"SELECT name || '@example.com' AS email FROM qry ORDER BY name",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.email[ 1 ] ).toBe( "alice@example.com" );
		expect( result.email[ 2 ] ).toBe( "bob@example.com" );
	}

	private function chainedConcat( opts ) {
		var qry = queryNew( "first,last", "varchar,varchar", [ [ "John", "Doe" ], [ "Jane", "Smith" ] ] );
		var result = queryExecute(
			"SELECT first || ' ' || last AS fullname FROM qry ORDER BY first",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.fullname[ 1 ] ).toBe( "Jane Smith" );
		expect( result.fullname[ 2 ] ).toBe( "John Doe" );
	}

	private function concatWithNull( opts ) {
		var qry = queryNew( "id,val", "integer,varchar" );
		queryAddRow( qry );
		querySetCell( qry, "id", 1 );
		querySetCell( qry, "val", "hello" );
		queryAddRow( qry );
		querySetCell( qry, "id", 2 );
		// val is NULL for row 2
		var result = queryExecute(
			"SELECT id, 'prefix_' || val || '_suffix' AS wrapped FROM qry ORDER BY id",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.recordcount ).toBe( 2 );
		expect( result.wrapped[ 1 ] ).toBe( "prefix_hello_suffix" );
		expect( result.wrapped[ 2 ] ).toBe( "prefix__suffix" );
	}

	private function concatWithEmpty( opts ) {
		var qry = queryNew( "id,val", "integer,varchar", [ [ 1, "" ], [ 2, "x" ] ] );
		var result = queryExecute(
			"SELECT id, '(' || val || ')' AS wrapped FROM qry ORDER BY id",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.wrapped[ 1 ] ).toBe( "()" );
		expect( result.wrapped[ 2 ] ).toBe( "(x)" );
	}

	private function concatInWhere( opts ) {
		var qry = queryNew( "code", "varchar", [ [ "A" ], [ "B" ], [ "C" ] ] );
		var result = queryExecute(
			"SELECT code FROM qry WHERE 'X' || code = 'XB'",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.recordcount ).toBe( 1 );
		expect( result.code ).toBe( "B" );
	}

	private function concatInOrderBy( opts ) {
		var qry = queryNew( "first,last", "varchar,varchar", [ [ "Z", "Alpha" ], [ "A", "Zeta" ] ] );
		var result = queryExecute(
			"SELECT first, last FROM qry ORDER BY last || first",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.first[ 1 ] ).toBe( "Z" );
		expect( result.first[ 2 ] ).toBe( "A" );
	}

	private function concatNumeric( opts ) {
		var qry = queryNew( "id,num", "integer,integer", [ [ 1, 42 ] ] );
		var result = queryExecute(
			"SELECT 'val=' || num AS label FROM qry",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.label ).toBe( "val=42" );
	}

	private function concatMixedWithPlus( opts ) {
		var qry = queryNew( "a,b", "integer,integer", [ [ 10, 20 ] ] );
		var result = queryExecute(
			"SELECT 'sum=' || (a + b) AS label FROM qry",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.label ).toBe( "sum=30" );
	}

	private function concatMultipleColumns( opts ) {
		var qry = queryNew( "a,b,c", "varchar,varchar,varchar", [ [ "x", "y", "z" ] ] );
		var result = queryExecute(
			"SELECT a || b || c AS combined FROM qry",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.combined ).toBe( "xyz" );
	}

}
