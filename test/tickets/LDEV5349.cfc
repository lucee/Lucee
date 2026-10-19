component extends="org.lucee.cfml.test.LuceeTestCase" labels="json" {

	function run( testResults, testBox ) {
		describe( "LDEV-5349 deserializeJSON( json, strictMapping='query' )", function() {

			var json = '[{"id":"1","name":"pothys"},{"id":"2","name":"lucee"}]';

			it( "returns an array of objects as a query", function() {
				var qry = deserializeJSON( json, "query" );
				expect( isQuery( qry ) ).toBeTrue();
				expect( qry.recordCount ).toBe( 2 );
				expect( queryColumnArray( qry ) ).toBe( [ "id", "name" ] );
				expect( qry.id[ 1 ] ).toBe( "1" );
				expect( qry.name[ 1 ] ).toBe( "pothys" );
				expect( qry.id[ 2 ] ).toBe( "2" );
				expect( qry.name[ 2 ] ).toBe( "lucee" );
			});

			it( "matches a query built with queryNew()", function() {
				var expected = queryNew( "id,name", "varchar,varchar", [ [ "1", "pothys" ], [ "2", "lucee" ] ] );
				expect( serializeJSON( deserializeJSON( json, "query" ) ) ).toBe( serializeJSON( expected ) );
			});

			it( "is case insensitive and works as named argument and member function", function() {
				expect( isQuery( deserializeJSON( json, "QUERY" ) ) ).toBeTrue();
				expect( isQuery( deserializeJSON( JSONVar=json, strictMapping="query" ) ) ).toBeTrue();
				expect( isQuery( json.deserializeJSON( "query" ) ) ).toBeTrue();
				expect( isQuery( deserializeJSON( json, "query", "json" ) ) ).toBeTrue();
			});

			it( "uses the keys of all rows as columns, missing values are null", function() {
				var qry = deserializeJSON( '[{"a":1,"b":2},{"b":3,"c":4}]', "query" );
				expect( queryColumnArray( qry ) ).toBe( [ "a", "b", "c" ] );
				expect( qry.recordCount ).toBe( 2 );
				expect( qry.b[ 2 ] ).toBe( 3 );
				expect( isNull( queryGetCell( qry, "a", 2 ) ) || queryGetCell( qry, "a", 2 ) == "" ).toBeTrue();
				expect( isNull( queryGetCell( qry, "c", 1 ) ) || queryGetCell( qry, "c", 1 ) == "" ).toBeTrue();
			});

			it( "keeps nested values as they are", function() {
				var qry = deserializeJSON( '[{"id":1,"tags":["a","b"],"meta":{"x":1}}]', "query" );
				expect( isArray( qry.tags[ 1 ] ) ).toBeTrue();
				expect( isStruct( qry.meta[ 1 ] ) ).toBeTrue();
				expect( qry.meta[ 1 ].x ).toBe( 1 );
			});

			it( "returns an empty query for an empty array", function() {
				var qry = deserializeJSON( '[]', "query" );
				expect( isQuery( qry ) ).toBeTrue();
				expect( qry.recordCount ).toBe( 0 );
			});

			it( "accepts the query formats of serializeJSON", function() {
				var src = queryNew( "id,name", "integer,varchar", [ [ 1, "pothys" ], [ 2, "lucee" ] ] );
				var qry = deserializeJSON( serializeJSON( src ), "query" );
				expect( isQuery( qry ) ).toBeTrue();
				expect( qry.recordCount ).toBe( 2 );
				expect( qry.name[ 2 ] ).toBe( "lucee" );
				qry = deserializeJSON( serializeJSON( src, "struct" ), "query" );
				expect( isQuery( qry ) ).toBeTrue();
				expect( qry.name[ 1 ] ).toBe( "pothys" );
			});

			it( "throws when the JSON is not an array of objects", function() {
				expect( function() { deserializeJSON( '{"a":1}', "query" ); } ).toThrow();
				expect( function() { deserializeJSON( '[1,2]', "query" ); } ).toThrow();
				expect( function() { deserializeJSON( '[{"a":1},"x"]', "query" ); } ).toThrow();
				expect( function() { deserializeJSON( '"abc"', "query" ); } ).toThrow();
			});

			it( "throws for an invalid strictMapping value", function() {
				expect( function() { deserializeJSON( json, "susi" ); } ).toThrow();
			});

			it( "true, false and the default still behave as before", function() {
				expect( isArray( deserializeJSON( json ) ) ).toBeTrue();
				expect( isArray( deserializeJSON( json, true ) ) ).toBeTrue();
				expect( isArray( deserializeJSON( json, false ) ) ).toBeTrue();
				expect( isArray( deserializeJSON( json, "true" ) ) ).toBeTrue();
				expect( isArray( deserializeJSON( json, "no" ) ) ).toBeTrue();
				var src = queryNew( "id", "integer", [ [ 1 ] ] );
				expect( isQuery( deserializeJSON( serializeJSON( src ), false ) ) ).toBeTrue();
				expect( isStruct( deserializeJSON( serializeJSON( src ), true ) ) ).toBeTrue();
			});
		});
	}
}
