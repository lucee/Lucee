component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-4445", function() {
			it(title = "QoQ should fail back to HSQLDB", body = function( currentSpec ) {
				var qry = queryNew( 'col', 'varchar', [['foo']] );

				var result = queryExecute(sql="
						SELECT ATAN( 5 )
						FROM qry
					"
					,params=[]
					,options={dbtype="query"}
				);

				expect( result ).toBeQuery();
				expect( result.recordCount ).toBe( 1 );
			});

		});
	}
}
