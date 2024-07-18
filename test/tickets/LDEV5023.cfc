component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults , testBox ) {

		describe( title='QofQ' , body=function(){

			it( title='QoQ select * from table same source table name HSQLDB', body=function() {
				var q = getTestQuery();
				var cols = replaceNoCase( q.columnList, ",unique", "" ); // cleanup reserved word
				// native engine
				cols = "name, id";
				var q_native = QueryExecute(
					sql = "SELECT #cols# FROM q",
					options = { dbtype: 'query', maxrows=5 }
				);
				var q_stash = duplicate( q_native );
				expect( q_stash.recordcount ).toBe( q_native.recordcount );
				// hsqldb engine, coz join
				var q_hsqlb = QueryExecute(
					sql = "SELECT t1.name FROM q_native t1, q_native t2 WHERE t1.id = t2.id",
					options = { dbtype: 'query' }
				);
				expect( q_stash.recordcount ).toBe( q_hsqlb.recordcount );
				expect( q_native.recordcount ).toBe( q_hsqlb.recordcount );
				expect( q_stash.recordcount ).toBe( q_native.recordcount );
			});

			it( title='QoQ select * from table same source table name (arguments) HSQLDB', body=function() {
				var q = getTestQuery();
				var cols = replaceNoCase( q.columnList, ",unique", "" ); // cleanup reserved word
				// native engine
				cols = "name, id";
				var q_native = QueryExecute(
					sql = "SELECT #cols# FROM q",
					options = { dbtype: 'query', maxrows=5 }
				);
				var q_stash = duplicate( q_native );
				// hsqldb engine, coz join
				arguments.q_native = q_native;
				var q_hsqlb = QueryExecute(
					sql = "SELECT t1.name FROM q_native t1, arguments.q_native t2 WHERE t1.id = t2.id",
					options = { dbtype: 'query' }
				);
				
				expect( q_stash.recordcount ).toBe( q_hsqlb.recordcount );
				expect( q_native.recordcount ).toBe( q_hsqlb.recordcount );
				expect( q_stash.recordcount ).toBe( q_native.recordcount );
			});

			it( title='QoQ select * from table same source table name (all cols) HSQLDB', body=function() {
				var q = getTestQuery();
				var cols = replaceNoCase( q.columnList, ",unique", "" ); // cleanup reserved word
				// native engine
				var q_native = QueryExecute(
					sql = "SELECT #cols# FROM q",
					options = { dbtype: 'query', maxrows=5 }
				);
				var q_stash = duplicate( q_native );
				// hsqldb engine, coz join
				var q_hsqlb = QueryExecute(
					sql = "SELECT t1.name FROM q_native t1, q_native t2 WHERE t1.id = t2.id",
					options = { dbtype: 'query' }
				);
				expect( q_stash.recordcount ).toBe( q_hsqlb.recordcount );
				expect( q_native.recordcount ).toBe( q_hsqlb.recordcount );
				expect( q_stash.recordcount ).toBe( q_native.recordcount );
			});

			it( title='QoQ select * from table same source table name (all cols) HSQLDB, 5000 threads', body=function() {

				var arr = [];
				ArraySet(arr, 1, 1000, 0);
				arrayEach(arr, function(){
					var q = getTestQuery();
					var cols = replaceNoCase( q.columnList, ",unique", "" ); // cleanup reserved word
					// native engine
					q = QueryExecute(
						sql = "SELECT #cols# FROM q",
						options = { dbtype: 'query' }
					);
					// hsqldb engine, coz join
					q = QueryExecute(
						sql = "SELECT t1.name FROM q t1, q t2 WHERE t1.id = t2.id",
						options = { dbtype: 'query' }
					);
				}, true);

			});
		});

	}

	private function getTestQuery(){
		return extensionList(); // has arrays and the like
		/*
		var q = queryNew("id,name,data","integer,varchar, varchar");
		loop list="micha,zac,brad,pothys,gert" item="local.n" index="local.i" {
			var r = queryAddRow( q );
			querySetCell(q, "id", r, r)
			querySetCell(q, "name", n, r)
			//querySetCell(q, "data", repeatString("lucee",1000), r);
		}
		return q;
		*/
	}

}