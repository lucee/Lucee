component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{

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
		describe( title='selecting 2 rows from QoQ' , body=function() {
			describe( title='is possible using a hard coded list' , body=function() {
				it( title='of numerics' , body=function( currentSpec ) {
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

				it( title='of strings' , body=function( currentSpec ) {
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

			describe( title='using param list=true' , body=function() {
				describe( title='with new Query()' , body=function() {
					beforeEach( function( currentSpec ) {
						q = new Query(
							dbtype = 'query',
							queryWithDataIn = variables.queryWithDataIn
						);
					});

					it( title='when using numeric params' , body=function( currentSpec ) {
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

					it( title='when using numeric params and a custom separator' , body=function( currentSpec ) {
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

					it( title='when using string params' , body=function( currentSpec ) {
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

				describe( title='with query{} ( cfquery )' , body=function() {
					it( title='when using numeric params' , body=function( currentSpec ) {
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

					it( title='when using numeric params and a custom separator' , body=function( currentSpec ) {
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

					it( title='when using string params' , body=function( currentSpec ) {
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

				describe( title='with QueryExecute' , body=function() {
					it( title='when using an array of numeric params' , body=function( currentSpec ) {
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

					it( title='when using a struct of numeric params' , body=function( currentSpec ) {
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

					it( title='when using an array of string params' , body=function( currentSpec ) {
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

					it( title='when using a struct of string params' , body=function( currentSpec ) {
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

					it( title='when using numeric params and a custom separator' , body=function( currentSpec ) {
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
	}
}