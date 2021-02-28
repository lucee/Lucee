component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {

		describe( 'QofQ' , function(){
			var t1 = queryNew("id,unique,and,order,by,table,type,select,distinct"); 
			var t1 = queryNew("id,zac,unique"); 
			var t2 = queryNew("id"); 
			var unique = t1; 
			var q = "";
			queryAddRow(t1);
			queryAddRow(t2);
			// add dummy data, force all cols to be varchar
			loop list="#t1.columnList()#" item="local.col" {
				querySetCell( t1, col, "lucee rocks", 1 );
			}
			querySetCell( t2, "id", "lucee rocks", 1 );

			it( 'QoQ select * from table with reserved word as column name with HSQLDB (one col)' , function() {
				// force fallback to hsqldb via join
				var q = QueryExecute(
					sql = "SELECT t1.unique FROM t1, t2 WHERE t1.id = t2.id",
					options = { dbtype: 'query' }
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe(1);
			});


			it( 'QoQ select * from table with reserved word as column name with HSQLDB' , function() {
				// force fallback to hsqldb via join
				var q = QueryExecute(
					sql = "SELECT t1.* FROM t1, t2 WHERE t1.id = t2.id",
					options = { dbtype: 'query' }
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe(1);
			});

			it( 'QoQ select * from table with reserved word as table name with HSQLDB' , function() {
				// force fallback to hsqldb via join

				expect(function(){
					var q = QueryExecute(
						sql = "SELECT UNIQUE.* FROM UNIQUE, t2 WHERE UNIQUE.id = t2.id",
						options = { dbtype: 'query' }
					);
				}).toThrow(); // reserved words need to be both DOUBLE QUOTED and in UPPER CASE

				var q = QueryExecute(
					sql = 'SELECT "UNIQUE".* FROM UNIQUE t2 WHERE "UNIQUE".id = t2.id',
					options = { dbtype: 'query' }
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe(1);
			});

			it( 'Qoq select * from table with reserved word as column name' , function() {
				var q = QueryExecute(
					sql = "SELECT t1.* FROM t1",
					options = { dbtype: 'query' }
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe(1);
			});

			it( 'Qoq select * from table with reserved word as table name' , function() {
				var q = QueryExecute(
					sql = 'SELECT "UNIQUE".* FROM UNIQUE', 
					options = { dbtype: 'query' }
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe(1);

				var q = QueryExecute(
					sql = 'SELECT UNIQUE.* FROM UNIQUE', 
					options = { dbtype: 'query' }
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe(1);
			});
			
		});

	}
	
} 