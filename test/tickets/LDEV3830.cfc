component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, textbox ) {

		describe("testcase for LDEV-3830", function(){

			it(title="QoQ distinct rows with dupes in same original result", body=function( currentSpec ){
				var a = queryNew("a","varchar");
				var b = queryNew("a","varchar", [['1'],['2'],['2']]);

				var actual = QueryExecute(
					sql = "select a from a union select a from b",
					options = { dbtype: 'query' }
				);
				
				expect( actual ).toBeQuery();
				expect( actual.recordCount ).toBe( 2 );

			});

		});

	}

}