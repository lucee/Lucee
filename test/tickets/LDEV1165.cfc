component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll() {
		variables.data = queryNew( "name", "varchar", [ [ "test_1" ], [ "test_2" ], [ "testA3" ], [ "other" ] ] );
	}

	function run( testResults, testBox ) {
		describe( "Test suite for LDEV-1165", function() {

			it( title="QoQ LIKE with ESCAPE, literal pattern", body=function( currentSpec ) {
				var q = queryExecute(
					"SELECT name FROM data WHERE name LIKE 'test\__' ESCAPE '\' ORDER BY name",
					{}, { dbtype: "query" }
				);
				expect( valueList( q.name ) ).toBe( "test_1,test_2" );
			});

			it( title="QoQ LIKE with ESCAPE, queryparam pattern", body=function( currentSpec ) {
				var q = queryExecute(
					"SELECT name FROM data WHERE name LIKE :p ESCAPE '\' ORDER BY name",
					{ p: { value: "test\__", sqltype: "varchar" } }, { dbtype: "query" }
				);
				expect( valueList( q.name ) ).toBe( "test_1,test_2" );
			});

			it( title="QoQ NOT LIKE with ESCAPE, literal pattern", body=function( currentSpec ) {
				var q = queryExecute(
					"SELECT name FROM data WHERE name NOT LIKE 'test\__' ESCAPE '\' ORDER BY name",
					{}, { dbtype: "query" }
				);
				expect( valueList( q.name ) ).toBe( "other,testA3" );
			});

			it( title="QoQ NOT LIKE with ESCAPE, queryparam pattern", body=function( currentSpec ) {
				var q = queryExecute(
					"SELECT name FROM data WHERE name NOT LIKE :p ESCAPE '\' ORDER BY name",
					{ p: { value: "test\__", sqltype: "varchar" } }, { dbtype: "query" }
				);
				expect( valueList( q.name ) ).toBe( "other,testA3" );
			});

			it( title="QoQ NOT LIKE without ESCAPE still works", body=function( currentSpec ) {
				var q = queryExecute(
					"SELECT name FROM data WHERE name NOT LIKE 'test%' ORDER BY name",
					{}, { dbtype: "query" }
				);
				expect( valueList( q.name ) ).toBe( "other" );
			});

			it( title="QoQ NOT LIKE with ESCAPE combined with AND", body=function( currentSpec ) {
				var q = queryExecute(
					"SELECT name FROM data WHERE name NOT LIKE 'test\__' ESCAPE '\' AND name LIKE 'test%' ORDER BY name",
					{}, { dbtype: "query" }
				);
				expect( valueList( q.name ) ).toBe( "testA3" );
			});

		});
	}

}
