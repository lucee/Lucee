component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function beforeAll() {
		variables.interestingNumbersAsAList = '3,4';
		variables.interestingStringsAsAList = "a,c,e";
		variables.interestingStringsAsAQuotedList = "'a','c','e'";

		variables.queryWithDataIn = Query(
			id: [ 1 , 2 , 3 , 4 , 5 ],
			value: [ 'a' , 'b' , 'c' , 'd' , 'e' ]
		);
	}

	function run( testResults , testBox ) {

		describe( 'selecting 2 rows from QoQ' , function() {

			describe( 'is possible using a hard coded list' , function() {

				it( 'of numerics' , function( currentSpec ) {

					var actual = QueryExecute(
						options = {
							dbtype: 'query'
						},
						sql = "
							SELECT 
								id,
								value
							FROM queryWithDataIn
							WHERE id IN ( "&interestingNumbersAsAList&" )
						"
					);

					expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' )  );

				});

				it( 'of strings' , function( currentSpec ) {

					var actual = QueryExecute(
						options = {
							dbtype: 'query'
						},
						sql = "
							SELECT 
								id,
								value
							FROM queryWithDataIn
							WHERE value IN ( "&interestingStringsAsAQuotedList&" )
						"
					);
					expect( actual.RecordCount ).toBe( ListLen( interestingStringsAsAQuotedList , ',' ) );

				});


			});

			describe( 'using param list=true' , function() {

				describe( 'with new Query()' , function() {

					beforeEach( function( currentSpec ) {
						q = new Query(
							dbtype = 'query',
							queryWithDataIn = variables.queryWithDataIn
						);
					});

					describe( 'returns expected rows' , function() {

						it( 'when using numeric params' , function( currentSpec ) {

							q.addParam( name: 'needle' , value: interestingNumbersAsAList , sqltype: 'numeric' , list: true );

							var actual = q.execute( sql = "
								SELECT 
									id,
									value
								FROM queryWithDataIn
								WHERE id IN ( :needle )
							" ).getResult();

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

						it( 'when using numeric params and a custom separator' , function( currentSpec ) {

							q.addParam( name: 'needle' , value: Replace( interestingNumbersAsAList , ',' , '|' ) , sqltype: 'numeric' , list: true , separator: '|' );

							var actual = q.execute( sql = "
								SELECT 
									id,
									value
								FROM queryWithDataIn
								WHERE id IN ( :needle )
							" ).getResult();

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

						it( 'when using string params' , function( currentSpec ) {

							q.addParam( name: 'needle' , value: interestingStringsAsAList , sqltype: 'varchar' , list: true );

							var actual = q.execute( sql = "
								SELECT 
									id,
									value
								FROM queryWithDataIn
								WHERE value IN ( :needle )
							" ).getResult();

							expect( actual.RecordCount ).toBe( ListLen( interestingStringsAsAList , ',' ) );

						});

					});

				});

				describe( 'with query{} ( cfquery )' , function() {

					describe( 'returns expected rows' , function() {

						it( 'when using numeric params' , function( currentSpec ) {

							query
								name = 'actual'
								dbtype = 'query' {

								WriteOutput( "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( "
								);

								queryparam
									value = interestingNumbersAsAList
									sqltype = 'integer'
									list = true;

								WriteOutput( " )" );
							}

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

						it( 'when using numeric params and a custom separator' , function( currentSpec ) {

							query
								name = 'actual'
								dbtype = 'query' {

								WriteOutput( "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( "
								);

								queryparam
									value = Replace( interestingNumbersAsAList , ',' , '|' )
									sqltype = 'integer'
									list = true
									separator = '|';

								WriteOutput( " )" );
							}

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

						it( 'when using string params' , function( currentSpec ) {

							query
								name = 'actual'
								dbtype = 'query' {

								WriteOutput( "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE value IN ( "
								);

								queryparam
									value = interestingStringsAsAList
									sqltype = 'varchar'
									list = true;

								WriteOutput( " )" );
							}

							expect( actual.RecordCount ).toBe( ListLen( interestingStringsAsAList , ',' ) );

						});
					});
				});

				describe( 'with QueryExecute' , function() {

					describe( 'returns expected rows' , function() {

						it( 'when using an array of numeric params' , function( currentSpec ) {

							var actual = QueryExecute(
								params = [
									{ name: 'needle' , value: interestingNumbersAsAList , sqltype: 'numeric' , list = true }
								],
								options = {
									dbtype: 'query'
								},
								sql = "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( :needle )
								"
							);

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});


						it( 'when using a struct of numeric params' , function( currentSpec ) {

							var actual = QueryExecute(
								params = {
									needle: { value: interestingNumbersAsAList , sqltype: 'numeric' , list: true }
								},
								options = {
									dbtype: 'query'
								},
								sql = "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( :needle )
								"
							);

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

						it( 'when using an array of string params' , function( currentSpec ) {

							var actual = QueryExecute(
								params = [
									{ name: 'needle' , value: interestingStringsAsAList , sqltype: 'varchar' , list = true }
								],
								options = {
									dbtype: 'query'
								},
								sql = "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE value IN ( :needle )
								"
							);

							expect( actual.RecordCount ).toBe( ListLen( interestingStringsAsAList , ',' ) );

						});


						it( 'when using a struct of string params' , function( currentSpec ) {

							var actual = QueryExecute(
								params = {
									needle: { value: interestingStringsAsAList , sqltype: 'varchar' , list: true }
								},
								options = {
									dbtype: 'query'
								},
								sql = "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE value IN ( :needle )
								"
							);

							expect( actual.RecordCount ).toBe( ListLen( interestingStringsAsAList , ',' ) );

						});


						it( 'when using numeric params and a custom separator' , function( currentSpec ) {

							var actual = QueryExecute(
								params = {
									needle: { value: Replace( interestingNumbersAsAList , ',' , '|' ) , sqltype: 'numeric' , list: true , separator: '|' }
								},
								options = {
									dbtype: 'query'
								},
								sql = "
									SELECT 
										id,
										value
									FROM queryWithDataIn
									WHERE id IN ( :needle )
								"
							);

							expect( actual.RecordCount ).toBe( ListLen( interestingNumbersAsAList , ',' ) );

						});

					});

				});

			});

		});


	}
	
	
} 