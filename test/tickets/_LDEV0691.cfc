component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-691", body=function() {
			it( title='Checking QuQ Count with structure data',body=function( currentSpec ) {
				var obj = { a=1, b=2, c=3 };
				var q_obj = QueryNew( "name,data,age", "varchar,varchar,Integer", [
			    [ "Susi", obj, 24 ],
			    [ "Urs" , "switz", 55 ],
			    [ "Fred", "India", 45 ],
			    [ "Jim" , "USA", 55 ]
				]);

				try {
					var qoq = QueryExecute(
						options = {
							dbtype: 'query'
						},
						sql = "
							select count(*) as r, age
							from q_obj
							group by age"
					);
					var result = qoq.recordCount
				} catch ( any e){
					var result = e.message;
				}

				expect( result ).toBe( 3 );
			});

			it( title='Checking QuQ Count with array data',body=function( currentSpec ) {
				var obj = [1,2,3,4];
				var q_obj = QueryNew( "name,data,age", "varchar,varchar,Integer", [
			    [ "Susi", obj, 24 ],
			    [ "Urs" , "switz", 55 ],
			    [ "Fred", "India", 45 ],
			    [ "Jim" , "USA", 55 ]
				]);

				try {
					var qoq = QueryExecute(
							options = {
								dbtype: 'query'
							},
							sql = "
								select count(*) as r, age
								from q_obj
								group by age"
					);
					var result = qoq.recordCount
				} catch ( any e){
					var result = e.message;
				}

				expect( result ).toBe( 3 );
			});

			it( title='Checking QuQ Count with query data',body=function( currentSpec ) {
				var obj = queryNew("test1, test2");
				var q_obj = QueryNew( "name,data,age", "varchar,varchar,Integer", [
			    [ "Susi", obj, 24 ],
			    [ "Urs" , "switz", 55 ],
			    [ "Fred", "India", 45 ],
			    [ "Jim" , "USA", 55 ]
				]);

				try {
					var qoq = QueryExecute(
							options = {
								dbtype: 'query'
							},
							sql = "
								select count(*) as r, age
								from q_obj
								group by age"
					);
					var result = qoq.recordCount
				} catch ( any e){
					var result = e.message;
				}

				expect( result ).toBe( 3 );
			});
		});
	}
}