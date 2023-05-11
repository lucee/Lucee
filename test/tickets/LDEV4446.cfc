component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults , testBox ) {

		describe( 'QoQ rand() function' , () =>{

			it( 'can generate totally random values' , ()=>{
				qry = queryNew( 'col', 'varchar', [['foo'],['bar']] );
				var actual = QueryExecute(
					sql = "
						SELECT  rand() rand
            			FROM qry",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 2 );
				expect( actual.rand[1] ).toBeBetween( 0, 1 );
				expect( actual.rand[2] ).toBeBetween( 0, 1 );
				expect( actual.rand[1] ).notToBe( actual.rand[2] );

			});

			it( 'can generate seeded random values' , ()=>{
				qry = queryNew( 'col', 'varchar', [['foo'],['bar']] );
				var actual = QueryExecute(
					sql = "
						SELECT  rand(3) rand, rand() rand2, rand() rand3
            			FROM qry",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 2 );
				expect( actual.rand[1] ).toBeBetween( 0, 1 );
				expect( actual.rand2[1] ).toBeBetween( 0, 1 );
				expect( actual.rand3[1] ).toBeBetween( 0, 1 );

				expect( actual.rand[1] ).toBe( actual.rand[2] );
				expect( actual.rand2[1] ).toBe( actual.rand2[2] );
				expect( actual.rand3[1] ).toBe( actual.rand3[2] );

			});

		});

	}


}