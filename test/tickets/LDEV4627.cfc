component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults , testBox ) {

		describe( 'QoQ rand() function' , () =>{

			it( 'can handle column names of different case' , ()=>{
				var qry = queryNew( 'col', 'varchar', [['foo'],['bar']] );
				var actual = QueryExecute(
					sql = "
						SELECT distinct col
						FROM qry
						where COL = 'foo'",
					options = { dbtype: 'query' }
				);

				expect( actual.recordcount ).toBe( 1 );

			});


		});

	}


}