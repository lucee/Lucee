component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV2487", function() {

			myquery = queryNew("name", "CF_SQL_VARCHAR");
			data = listToArray("Copay Plan With Wellness,Copay Plan Without Wellness");
			count = arrayLen(data);
			for (i = 1; i <= count; i++)
			queryAddRow(myquery, {name: data[i]});
			
			it(title = "checking with varchar data type - desc", body = function( currentSpec ) {
				q = queryExecute("SELECT * FROM myquery ORDER BY name desc", {}, {dbType: "query"});
				expect(q.name[1]).toBe('Copay Plan Without Wellness');
			});

			it(title = "checking with varchar data type - asc", body = function( currentSpec ) {
				q = queryExecute("SELECT * FROM myquery ORDER BY name asc", {}, {dbType: "query"});
				expect(q.name[1]).toBe('Copay Plan With Wellness');
			});

			it(title = "checking querySort - desc", body = function( currentSpec ) {
				var q =duplicate(myquery);
				querySort(q,"name","desc");
				expect(q.name[1]).toBe('Copay Plan Without Wellness');
			});

			it(title = "checking querySort - asc", body = function( currentSpec ) {
				var q =duplicate(myquery);
				querySort(q,"name","asc");
				expect(q.name[1]).toBe('Copay Plan With Wellness');

			});
		});
	}
}