component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function beforeAll() {
		variables.testData = Query(
			id: [ 1, 2, 3, 4, 5 ],
			value: [ 'a', 'b', 'c', 'd', 'e' ]
		);
	}

	function run( testResults, testBox ) {

		describe( 'query.addParam with list=true', function() {

			describe( 'using array as value', function() {

				it( 'should work with numeric array', function() {
					var q = new Query(
						dbtype = 'query',
						testData = variables.testData
					);
					q.addParam( name: 'ids', value: [ 1, 3, 5 ], sqltype: 'integer', list: true );

					var result = q.execute( sql = "
						SELECT id, value
						FROM testData
						WHERE id IN ( :ids )
					" ).getResult();

					expect( result.RecordCount ).toBe( 3 );
					expect( valueList( result.id ) ).toBe( '1,3,5' );
				});

				it( 'should work with string array', function() {
					var q = new Query(
						dbtype = 'query',
						testData = variables.testData
					);
					q.addParam( name: 'vals', value: [ 'a', 'c', 'e' ], sqltype: 'varchar', list: true );

					var result = q.execute( sql = "
						SELECT id, value
						FROM testData
						WHERE value IN ( :vals )
					" ).getResult();

					expect( result.RecordCount ).toBe( 3 );
					expect( valueList( result.value ) ).toBe( 'a,c,e' );
				});

				it( 'should work with positional array param', function() {
					var q = new Query(
						dbtype = 'query',
						testData = variables.testData
					);
					q.addParam( value: [ 2, 4 ], sqltype: 'integer', list: true );

					var result = q.execute( sql = "
						SELECT id, value
						FROM testData
						WHERE id IN ( ? )
					" ).getResult();

					expect( result.RecordCount ).toBe( 2 );
					expect( valueList( result.id ) ).toBe( '2,4' );
				});

			});

			describe( 'using string list as value (existing behaviour)', function() {

				it( 'should work with comma-separated string', function() {
					var q = new Query(
						dbtype = 'query',
						testData = variables.testData
					);
					q.addParam( name: 'ids', value: '1,3,5', sqltype: 'integer', list: true );

					var result = q.execute( sql = "
						SELECT id, value
						FROM testData
						WHERE id IN ( :ids )
					" ).getResult();

					expect( result.RecordCount ).toBe( 3 );
				});

				it( 'should throw on empty string value', function() {
					expect( function() {
						var q = new Query(
							dbtype = 'query',
							testData = variables.testData
						);
						q.addParam( name: 'ids', value: '', sqltype: 'integer', list: true );
					}).toThrow();
				});

				it( 'should throw on whitespace-only string value', function() {
					expect( function() {
						var q = new Query(
							dbtype = 'query',
							testData = variables.testData
						);
						q.addParam( name: 'ids', value: '   ', sqltype: 'integer', list: true );
					}).toThrow();
				});

			});

			describe( 'edge cases with arrays', function() {

				it( 'should throw on empty array value', function() {
					expect( function() {
						var q = new Query(
							dbtype = 'query',
							testData = variables.testData
						);
						q.addParam( name: 'ids', value: [], sqltype: 'integer', list: true );
					}).toThrow();
				});

			});

		});

	}

}
