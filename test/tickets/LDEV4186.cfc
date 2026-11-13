component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" {
	function run( testResults, textbox ) {
		describe("testcase for LDEV-4186", function() {
			variables.myQuery = queryNew( "id,name","CF_SQL_integer,CF_SQL_varchar");
				queryAddRow(myQuery, {id:1, name:'011'});
				queryAddRow(myQuery, {id:2, name:'11'});
				queryAddRow(myQuery, {id:3, name:'    123'}); // column value with trailing space
				queryAddRow(myQuery, {id:4, name:'lucee '}); // column value with trailing space

			it(title = "Checking QoQ string column with numeric values", body = function ( currentSpec ) {

				cfquery(name="queryResult", dbtype="query") {
					echo(" select * from myQuery where name='011' ");
				}

				cfquery(name="queryRes", dbtype="query") {
					echo(" select * from myQuery where name='11' ");
				}

				expect(queryResult.recordcount).toBe("1");
				expect(queryColumnData( queryResult,'name' ).toList()).tobe("011");

				expect(queryRes.recordcount).toBe("1");
				expect(queryColumnData( queryRes,'name' ).toList()).tobe("11");
			});

			it(title = "Checking QoQ string column with trailing space", body = function ( currentSpec ) {
				
				cfquery(name="queryresult1", dbtype="query") {
					echo(" select * from myQuery where name='lucee ' ");
				}

				cfquery(name="queryresult2", dbtype="query") { 
					echo(" select * from myQuery where name='    lucee   ' ");
				}

				cfquery(name="queryresult3", dbtype="query") {
					echo(" select * from myQuery where name='123' ");
				}

				expect(queryresult1.recordcount).toBe("1");
				expect(queryresult2.recordcount).toBe("0");
				expect(queryresult3.recordcount).toBe("0");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}