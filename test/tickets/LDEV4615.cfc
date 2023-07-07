component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, testBox ){

		describe( "test hsqldb qoq support ", function(){

			it( "QoQ: data exception: string data, right truncation ; size limit: 1", function(){

				var q1 = queryNew(
					"id,title,inits","integer,varchar,char",
					{"id":1,"title":"test","inits":"AK"}
				);

				var q2 = queryNew(
					"id2,title2","integer,varchar",
					{"id2":2,"title2":"test"}
				);
				
				var q= queryExecute( "SELECT * FROM q1, q2 WHERE	q1.title = q2.title2", {}, { dbtype="query" } );

				expect ( q.recordcount ).toBe( 1 );
			});


		} );
	}

}
