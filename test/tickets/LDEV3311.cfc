component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function run( testResults , testBox ) {

		describe( title='QofQ' , body=function(){
			var t1 = queryNew( "id,unique,and,order,by,table,type,select,distinct" ); 
			var t1 = queryNew( "id,zac,unique" ); 
			var t2 = queryNew( "id" ); 
			var unique = t1; 
			var q = "";
			queryAddRow( t1 );
			queryAddRow( t2 );
			// add dummy data, force all cols to be varchar
			loop list="#t1.columnList()#" item="local.col" {
				querySetCell( t1, col, "lucee rocks", 1 );
			}
			querySetCell( t2, "id", "lucee rocks", 1 );

			it( title='QoQ select * from table with reserved word as column name with HSQLDB (one col)' , body=function() {
				// force fallback to hsqldb via join
				var q = QueryExecute(
					sql = "SELECT t1.unique FROM t1, t2 WHERE t1.id = t2.id",
					options = { dbtype: 'query' }
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe( 1 );
			});


			it( title='QoQ select * from table with reserved word as column name with HSQLDB' , body=function() {
				// force fallback to hsqldb via join
				var q = QueryExecute(
					sql = "SELECT t1.* FROM t1, t2 WHERE t1.id = t2.id",
					options = { dbtype: 'query' }
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe( 1 );
			});

			it( title='QoQ select * from table with reserved word as table name with HSQLDB', skip=true, body=function() {
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
				expect( q.recordcount ).toBe( 1 );
			});

			it( title='Qoq select * from table with reserved word as column name' , body=function() {
				var q = QueryExecute(
					sql = "SELECT t1.* FROM t1",
					options = { dbtype: 'query' }
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe( 1 );
			});

			it( title='Qoq select * from table with reserved word as table name' , body=function() {
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
				expect( q.recordcount ).toBe( 1 );
			});

			it( title='Qoq select * from 100k row table' , body=function() {
				var q1 = extensionList();
				// strip out complex columns (works ok in java 11 but fails on 8)
				q1 = QueryExecute(
					sql = 'SELECT ID,VERSION,NAME,SYMBOLICNAME,TYPE,DESCRIPTION,IMAGE,RELEASETYPE,TRIAL,STARTBUNDLES from q1', 
					options = { dbtype: 'query' }
				);
				var q2 = QueryNew("id");
				loop times=100000 {
					local.r = QueryAddRow( q1 );
					QuerySetRow(q1, r, QueryRowData( q1, 1 ));
				}
				var q = QueryExecute(
					sql = 'SELECT q1.id FROM q2, q1 where q1.id = q2.id', 
					options = { dbtype: 'query' }
				);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe( 0 );
			});
			
		});

	}
	
} 