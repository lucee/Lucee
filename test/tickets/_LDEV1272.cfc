component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
	function beforeAll(){
		variables.test = QueryNew("id,title,name");
		QueryAddRow(test, 4);
		QuerySetCell(test, "id", 1, 1);
		QuerySetCell(test, "title", "event 1", 1);
		QuerySetCell(test, "name", "dave", 1);
		QuerySetCell(test, "id", 1, 2);
		QuerySetCell(test, "title", "event 1", 2);
		QuerySetCell(test, "name", "john", 2);
		QuerySetCell(test, "id", 2, 3);
		QuerySetCell(test, "title", "event 2", 3);
		QuerySetCell(test, "name", "pete", 3);
		QuerySetCell(test, "id", 3, 4);
		QuerySetCell(test, "title", "event 3", 4);
		QuerySetCell(test, "name", "dchris", 4);
	}
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1272", function() {
			it( title='Checking DISTINCT query with maxrows attribute', body=function( currentSpec ) {
				query name="test1" dbtype="query" maxrows="2"{
					echo("SELECT DISTINCT id, title FROM test");
				};
				expect(valueList(test1.id)).toBe("1,2");
				expect(valueList(test1.title)).toBe("event 1,event 2");
				expect(test1.recordcount).toBe(2);
			});

			it( title='Checking GROUP BY query with maxrows attribute', body=function( currentSpec ) {
				query name="test2" dbtype="query" maxrows="2"{
					echo("SELECT count(*) as cnt, id, title FROM test group by id, title");
				};
				expect(valueList(test2.cnt)).toBe("2,1");
				expect(valueList(test2.id)).toBe("1,2");
				expect(valueList(test2.title)).toBe("event 1,event 2");
				expect(test2.recordcount).toBe(2);
			});
		});
	}
}
