component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults , testBox ) {

		describe( 'check column case with qoq' , () =>{

			it( 'column case is preserved (mixed)' , ()=>{
				var qMaster = queryNew( 'ID, THours', 'numeric,numeric', [[1000, 6],[1000, 5]] );
				var actual = QueryExecute(
					sql = "
						SELECT ID, sum(THours) AS THours
						FROM qMaster
						GROUP BY id",
					options = { dbtype: 'query' }
				);

				expect( actual.recordcount ).toBe( 1 );
				expect( actual.thours ).toBe( 11 );
				expect( listFirst( actual.columnList ,"," ) ).toBeWithCase( 'ID' );

			});

			it( 'column case is preserved (same)' , ()=>{
				var qMaster = queryNew( 'ID, THours', 'numeric,numeric', [[1000, 6],[1000, 5]] );
				var actual = QueryExecute(
					sql = "
						SELECT ID, sum(THours) AS THours
						FROM qMaster
						GROUP BY ID",
					options = { dbtype: 'query' }
				);

				expect( actual.recordcount ).toBe( 1 );
				expect( actual.thours ).toBe( 11 );
				expect( listFirst( actual.columnList ,"," ) ).toBeWithCase( 'ID' );

			});


		});

	}


}