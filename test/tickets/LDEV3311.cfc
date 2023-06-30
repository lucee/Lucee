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
			/*
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
			*/
			it( title='Qoq select join from large tables' , body=function() {
				//var src = extensionList();

				var src = getDummyData();
				
				//debug(QueryColumnData( query=q1, columnName="name"));
				// strip out complex columns (works ok in java 11 but fails on 8)
				/*q1 = QueryExecute(
					sql = 'SELECT #src.columnList#ID,VERSION,NAME,SYMBOLICNAME,TYPE,DESCRIPTION,RELEASETYPE,TRIAL,STARTBUNDLES from src', 
					options = { dbtype: 'query', maxrows=5 }
				);
				*/
				q1 = QueryExecute(
					sql = 'SELECT #src.columnList# from src', 
					options = { dbtype: 'query', maxrows=5 }
				);
				//debug(q1);
				systemOutput("q1 has #q1.recordcount# rows", true);
				//var q2 = extensionList();
				var q2 = getDummyData();
				var count=1024*1024;
				loop times=#count# {
					local.r = QueryAddRow( q2 );
					QuerySetRow(q2, r, QueryRowData( q1, 1 ));
				}
				systemOutput("q2 has #q2.recordcount# rows", true);

				var q1check = QueryExecute(
					sql = 'SELECT q1.id FROM q1 where id in (select id from q1 )',
					options = { dbtype: 'query' }
				);
				expect( q1check.recordcount ).toBe( q1.recordcount );
				//debug(q2);
				var q2check = QueryExecute(
					sql = 'SELECT q2.id FROM q2 where id in (select id from q2 )',
					options = { dbtype: 'query' }
				);

				expect( q2check.recordcount ).toBe( q2.recordcount );

				var q = QueryExecute(
					sql = 'SELECT q1.id, q2.id as id2 FROM q2, q1 where q1.id = q2.id group by q1.id, q2.id',
					options = { dbtype: 'query' }
				);

				//debug(q);
				expect( q ).toBeQuery();
				expect( q.recordcount ).toBe( 5 ); 
			});
			
		});

	}

	private function getDummyData (){
		var q = queryNew("id,name,data","integer,varchar, varchar");
		loop list="micha,zac,brad,pothys,gert" item="n" index="i" {
			var r = queryAddRow(q);
			querySetCell(q, "id", r, r)
			querySetCell(q, "name", n, r)
			querySetCell(q, "data", repeatString("lucee",1000), r);
		}
		return q;
	}
	
} 