component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll() {
		variables.queryWithDataIn = Query(
			id: [ 1 ]
		);
	}

	function run( testResults , testBox ) {

		describe( 'QueryExecute' , function(){

			it( 'returns NULL when fed in through array parameter with nulls=true' , function() {

				/*
					I have no idea *why* this is this way, but in the source code I spotted this.
					It turns out that there is an ability to cast to null but the parameter attribute
					is "nulls" instead of "null", go figure!
				*/

				actual = QueryExecute(
					options = {
						dbtype: 'query'
					},
					params = [
						{ type: 'integer' , value: 1 , nulls: true }
					],
					sql = "
						SELECT 
							COALESCE( ? , 'isnull' ) AS value,
							COALESCE( NULL , 'isnull' ) AS control
						FROM queryWithDataIn
					"
				);

				expect( actual.control[1] ).toBe( 'isnull' );
				expect( actual.value[1] ).toBe( 'isnull' );

			});

			it( 'returns NULL when fed in through array parameter with null=true' , function() {

				actual = QueryExecute(
					options = {
						dbtype: 'query'
					},
					params = [
						{ type: 'integer' , value: 1 , null: true }
					],
					sql = "
						SELECT 
							COALESCE( ? , 'isnull' ) AS value,
							COALESCE( NULL , 'isnull' ) AS control
						FROM queryWithDataIn
					"
				);

				expect( actual.control[1] ).toBe( 'isnull' );
				expect( actual.value[1] ).toBe( 'isnull' );

			});

			it( 'returns NULL when fed in through array named parameter with null=true' , function() {

				actual = QueryExecute(
					options = {
						dbtype: 'query'
					},
					params = [
						{ name: 'input' , type: 'integer' , value: 1 , null: true }
					],
					sql = "
						SELECT 
							COALESCE( :input , 'isnull' ) AS value,
							COALESCE( NULL , 'isnull' ) AS control
						FROM queryWithDataIn
					"
				);

				expect( actual.control[1] ).toBe( 'isnull' );
				expect( actual.value[1] ).toBe( 'isnull' );

			});

			it( 'returns NULL when fed in through struct parameter with null=true' , function() {

				actual = QueryExecute(
					options = {
						dbtype: 'query'
					},
					params = {
						'input': { type: 'integer' , value: 1 , null: true }
					},
					sql = "
						SELECT 
							COALESCE( :input , 'isnull' ) AS value,
							COALESCE( NULL , 'isnull' ) AS control
						FROM queryWithDataIn
					"
				);

				expect( actual.control[1] ).toBe( 'isnull' );
				expect( actual.value[1] ).toBe( 'isnull' );


			});

		});

		describe( 'new Query' , function(){

			it( 'returns NULL when fed in through parameter with null=true' , function() {

				q = new Query(
					dbtype = 'query',
					queryWithDataIn = variables.queryWithDataIn
				);

				q.addParam( type: 'integer' , value: 1 , null: true );

				actual = q.execute( 
					sql = "
						SELECT 
							COALESCE( ? , 'isnull' ) AS value,
							COALESCE( NULL , 'isnull' ) AS control
						FROM queryWithDataIn
					"
				).getResult();

				expect( actual.control[1] ).toBe( 'isnull' );
				expect( actual.value[1] ).toBe( 'isnull' );

			});

			it( 'returns NULL when fed in through named parameter with null=true' , function() {

				q = new Query(
					dbtype = 'query',
					queryWithDataIn = variables.queryWithDataIn
				);

				q.addParam( name: 'input' , type: 'integer' , value: 1 , null: true );

				actual = q.execute( 
					sql = "
						SELECT 
							COALESCE( :input , 'isnull' ) AS value,
							COALESCE( NULL , 'isnull' ) AS control
						FROM queryWithDataIn
					"
				).getResult();

				expect( actual.control[1] ).toBe( 'isnull' );
				expect( actual.value[1] ).toBe( 'isnull' );

			});

		});

		describe( 'cfquery in script' , function(){

			it( 'returns NULL when fed in through parameter with null=true' , function() {

				query
					name = 'actual'
					dbtype = 'query' {

					WriteOutput( "

						SELECT 
							COALESCE( 
					" );

					queryparam
						value = 1
						sqltype = 'integer'
						null = true;

					WriteOutput( " , 'isnull' ) AS value,
							COALESCE( NULL , 'isnull' ) AS control
						FROM queryWithDataIn
					" );
				}

				expect( actual.control[1] ).toBe( 'isnull' );
				expect( actual.value[1] ).toBe( 'isnull' );

			});

		});

	}
	
	
} 