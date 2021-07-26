component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function beforeAll() {
		q = queryNew(
			'type,num',
			'string,int',
			[
				['test',1],
				['test',2],
				['foo',1]
			]
		);

	}

	function run( testResults , testBox ) {

		describe( 'QofQ Aggregation' , function(){

			it( 'Can can aggregate unflitered rows' , function() {
				actual = QueryExecute(
					sql = "SELECT sum(num) as sum, count(*) as count, 4 as brad FROM q",
					options = { dbtype: 'query' }
				);
				expect( actual ).toBeQuery();
				expect( actual.recordcount ).toBe( 1 );
				expect( actual.sum ).toBe( 4 );
				expect( actual.count ).toBe( 3 );
				expect( actual.brad ).toBe( 4 );
			});

			it( 'Can can aggregate some filtered rows' , function() {
				actual = QueryExecute(
					sql = "SELECT sum(num) as sum, count(*) as count, 4 as brad FROM q WHERE type='test'",
					options = { dbtype: 'query' }
				);
				expect( actual ).toBeQuery();
				expect( actual.recordcount ).toBe( 1 );
				expect( actual.sum ).toBe( 3 );
				expect( actual.count ).toBe( 2 );
				expect( actual.brad ).toBe( 4 );
			});

			it( 'Can can aggregate all flitered rows' , function() {
				actual = QueryExecute(
					sql = "SELECT sum(num) as sum, count(*) as count, 4 as brad FROM q WHERE 1=0",
					options = { dbtype: 'query' }
				);
				expect( actual ).toBeQuery();
				expect( actual.recordcount ).toBe( 1 );
				expect( actual.sum ).toBe( '' );
				expect( actual.count ).toBe( 0 );
				expect( actual.brad ).toBe( 4 );
			});

		});

	}
	
	
} 