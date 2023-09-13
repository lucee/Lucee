component extends="org.lucee.cfml.test.LuceeTestCase"	labels="qoq" {

	function run( testResults , testBox ) {

		describe( title='QofQ' , body=function(){

			it( title='QoQ check decimal types scale are preserved' , body=function() {
						var dec = 5.57;
						var q1 = QueryNew( "id,dec", "integer,decimal" );
						q1.addRow( { id: 1, dec: dec } );
						var q2 = QueryNew( "id,str", "integer,varchar" );
						q2.addRow( { id: 1, str: "testing" } );

						var q3sql = "
							select  q1.*
							from    q1, q2
							where   q1.id = q2.id
						";
						var q3 = QueryExecute( q3sql, {}, {dbtype: "query"} );
						// q3.dec should be 5.57. It was 6 instead.
						expect( q3.dec ).toBe( dec );

						var q4sql = "
							select  *
							from    q1, q2
							where   q1.id = q2.id
						";
						var q4 = QueryExecute( q4sql, {}, {dbtype: "query"} );
						// q4.dec should be 5.57. It was 6 instead.
						expect( q4.dec ).toBe( dec );
			});
		});

	}

} 