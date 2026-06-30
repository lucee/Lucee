component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll() {
		variables.src = queryNew( "id,campo", "integer,varchar" );
		queryAddRow( variables.src );
		querySetCell( variables.src, "id", 1 );
		querySetCell( variables.src, "campo", "" );
		queryAddRow( variables.src );
		querySetCell( variables.src, "id", 2 );
		querySetCell( variables.src, "campo", "123" );
		queryAddRow( variables.src );
		querySetCell( variables.src, "id", 3 );
		// campo is NULL for row 3
		queryAddRow( variables.src );
		querySetCell( variables.src, "id", 4 );
		querySetCell( variables.src, "campo", "456" );

		variables.nativeQoQ = { dbtype: { type: "query", engine: "native" } };
		variables.hsqldbQoQ = { dbtype: { type: "query", engine: "hsqldb" } };
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6154 QoQ || concat with NULL/empty - native engine", function() {

			it( title="|| concat SELECT returns all rows including NULL", body=function() {
				concatOperatorSelectReturnsAllRows( variables.nativeQoQ );
			});

			it( title="|| concat WHERE NOT LIKE does not filter out NULL/empty rows", body=function() {
				concatOperatorWhereNotLikeReturnsThreeRows( variables.nativeQoQ );
			});

			it( title="CONCAT() SELECT returns all rows including NULL", body=function() {
				concatFunctionSelectReturnsAllRows( variables.nativeQoQ );
			});

			it( title="CONCAT() WHERE NOT LIKE does not filter out NULL/empty rows", body=function() {
				concatFunctionWhereNotLikeReturnsThreeRows( variables.nativeQoQ );
			});

		});

		describe( "LDEV-6154 QoQ || concat with NULL/empty - HSQLDB engine", function() {

			it( title="|| concat SELECT returns all rows including NULL", body=function() {
				concatOperatorSelectReturnsAllRows( variables.hsqldbQoQ );
			});

			it( title="|| concat WHERE NOT LIKE does not filter out NULL/empty rows", body=function() {
				concatOperatorWhereNotLikeReturnsThreeRows( variables.hsqldbQoQ );
			});

			it( title="CONCAT() SELECT returns all rows including NULL", body=function() {
				concatFunctionSelectReturnsAllRows( variables.hsqldbQoQ );
			});

			it( title="CONCAT() WHERE NOT LIKE does not filter out NULL/empty rows", body=function() {
				concatFunctionWhereNotLikeReturnsThreeRows( variables.hsqldbQoQ );
			});

		});

	}

	private function concatOperatorSelectReturnsAllRows( opts ) {
		var src = variables.src;
		var result = queryExecute(
			"SELECT id, ',' || campo || ',' AS result FROM src ORDER BY id",
			{},
			opts
		);
		//systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.recordcount ).toBe( 4 );
		expect( result.result[ 1 ] ).toBe( ",," );
		expect( result.result[ 2 ] ).toBe( ",123," );
		expect( result.result[ 3 ] ).toBe( ",," );
		expect( result.result[ 4 ] ).toBe( ",456," );
	}

	private function concatOperatorWhereNotLikeReturnsThreeRows( opts ) {
		var src = variables.src;
		var result = queryExecute(
			"SELECT * FROM src WHERE ',' || campo || ',' NOT LIKE '%,123,%' ORDER BY id",
			{},
			opts
		);
		//systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.recordcount ).toBe( 3 );
		expect( result.id[ 1 ] ).toBe( 1 );
		expect( result.id[ 2 ] ).toBe( 3 );
		expect( result.id[ 3 ] ).toBe( 4 );
	}

	private function concatFunctionSelectReturnsAllRows( opts ) {
		var src = variables.src;
		var result = queryExecute(
			"SELECT id, CONCAT( ',', campo, ',' ) AS result FROM src ORDER BY id",
			{},
			opts
		);
		//systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.recordcount ).toBe( 4 );
		expect( result.result[ 1 ] ).toBe( ",," );
		expect( result.result[ 2 ] ).toBe( ",123," );
		expect( result.result[ 3 ] ).toBe( ",," );
		expect( result.result[ 4 ] ).toBe( ",456," );
	}

	private function concatFunctionWhereNotLikeReturnsThreeRows( opts ) {
		var src = variables.src;
		var result = queryExecute(
			"SELECT * FROM src WHERE CONCAT( ',', campo, ',' ) NOT LIKE '%,123,%' ORDER BY id",
			{},
			opts
		);
		//systemOutput( serializeJson( var=result, compact=false ), true );
		expect( result.recordcount ).toBe( 3 );
		expect( result.id[ 1 ] ).toBe( 1 );
		expect( result.id[ 2 ] ).toBe( 3 );
		expect( result.id[ 3 ] ).toBe( 4 );
	}

}
