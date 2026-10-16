component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll() {
		variables.nativeQoQ = { dbtype: { type: "query", engine: "native" } };
		variables.hsqldbQoQ = { dbtype: { type: "query", engine: "hsqldb" } };
	}

	function run( testResults, testBox ) {

		describe( "LDEV-4182 QoQ CONCAT() with unlimited params - native engine", function() {

			it( title="CONCAT() with 2 args", body=function() {
				concat2Args( variables.nativeQoQ );
			});

			it( title="CONCAT() with 3 args", body=function() {
				concat3Args( variables.nativeQoQ );
			});

			it( title="CONCAT() with 4 args", body=function() {
				concat4Args( variables.nativeQoQ );
			});

			it( title="CONCAT() with 5 args", body=function() {
				concat5Args( variables.nativeQoQ );
			});

			it( title="CONCAT() with NULL args", body=function() {
				concatWithNull( variables.nativeQoQ );
			});

			it( title="CONCAT() with columns and literals mixed", body=function() {
				concatMixed( variables.nativeQoQ );
			});

			it( title="CONCAT() in WHERE clause", body=function() {
				concatInWhere( variables.nativeQoQ );
			});

		});

		describe( "LDEV-4182 QoQ CONCAT() with unlimited params - HSQLDB engine", function() {

			it( title="CONCAT() with 2 args", body=function() {
				concat2Args( variables.hsqldbQoQ );
			});

			it( title="CONCAT() with 3 args", body=function() {
				concat3Args( variables.hsqldbQoQ );
			});

			it( title="CONCAT() with 4 args", body=function() {
				concat4Args( variables.hsqldbQoQ );
			});

			it( title="CONCAT() with 5 args", body=function() {
				concat5Args( variables.hsqldbQoQ );
			});

			it( title="CONCAT() with NULL args", body=function() {
				concatWithNull( variables.hsqldbQoQ );
			});

			it( title="CONCAT() with columns and literals mixed", body=function() {
				concatMixed( variables.hsqldbQoQ );
			});

			it( title="CONCAT() in WHERE clause", body=function() {
				concatInWhere( variables.hsqldbQoQ );
			});

		});

	}

	private function concat2Args( opts ) {
		var qry = queryNew( "id", "integer", [ [ 1 ] ] );
		var result = queryExecute(
			"SELECT CONCAT( 'foo', 'bar' ) AS val FROM qry",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.val ).toBe( "foobar" );
	}

	private function concat3Args( opts ) {
		var qry = queryNew( "id", "integer", [ [ 1 ] ] );
		var result = queryExecute(
			"SELECT CONCAT( 'a', 'b', 'c' ) AS val FROM qry",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.val ).toBe( "abc" );
	}

	private function concat4Args( opts ) {
		var qry = queryNew( "id", "integer", [ [ 1 ] ] );
		var result = queryExecute(
			"SELECT CONCAT( 'a', 'b', 'c', 'd' ) AS val FROM qry",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.val ).toBe( "abcd" );
	}

	private function concat5Args( opts ) {
		var qry = queryNew( "id", "integer", [ [ 1 ] ] );
		var result = queryExecute(
			"SELECT CONCAT( 'a', 'b', 'c', 'd', 'e' ) AS val FROM qry",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.val ).toBe( "abcde" );
	}

	private function concatWithNull( opts ) {
		var qry = queryNew( "id,val", "integer,varchar" );
		queryAddRow( qry );
		querySetCell( qry, "id", 1 );
		// val is NULL
		var result = queryExecute(
			"SELECT CONCAT( 'pre_', val, '_post' ) AS wrapped FROM qry",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.wrapped ).toBe( "pre__post" );
	}

	private function concatMixed( opts ) {
		var qry = queryNew( "first,last", "varchar,varchar", [ [ "John", "Doe" ], [ "Jane", "Smith" ] ] );
		var result = queryExecute(
			"SELECT CONCAT( first, ' ', last, ' (', first, ')' ) AS label FROM qry ORDER BY first",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.label[ 1 ] ).toBe( "Jane Smith (Jane)" );
		expect( result.label[ 2 ] ).toBe( "John Doe (John)" );
	}

	private function concatInWhere( opts ) {
		var qry = queryNew( "id,code", "integer,varchar", [ [ 1, "A" ], [ 2, "B" ], [ 3, "C" ] ] );
		var result = queryExecute(
			"SELECT id FROM qry WHERE CONCAT( 'X', code, 'Y' ) = 'XBY'",
			{},
			opts
		);
		systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.recordcount ).toBe( 1 );
		expect( result.id ).toBe( 2 );
	}

}
