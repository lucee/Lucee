component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, testBox ){

		describe( "test query duplicate ", function(){

			it( "test query duplicate", function(){

				var q = queryNew("id,name","integer,varchar");
				loop list="micha,zac,brad,pothys" item="local.n" {
					var r = queryAddRow(q);
					querySetCell( q, "id", r, r );
					querySetCell( q, "name", n, r);
				}
				var recs = q.recordCount;

				var q2 = duplicate( q );

				loop times=2 {
					queryAddRow( q2 );
				}

				expect( q.recordcount ).toBe( recs );
				expect( q2.recordcount ).notToBe( recs );
				expect( q2.recordcount ).notToBe( q.recordcount );
			});

		} );
	}

}
