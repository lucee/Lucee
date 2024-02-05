component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	function run( testResults , testBox ) {
		describe( "testcase for LDEV-4695", function() {
			it( title="Checking QoQ IN operator for LDEV-4695", body=function( currentSpec ) {
				myQuery = QueryNew( "navid, type, url","decimal,decimal,VarChar", [ 
				{"navid": 200,"type": 1,"url": "football"},
				{"navid": 20010,"type": 2,"url": "offense"}
				]);

				expect(function() { 
					query name="myNewQuery" dbtype="query" {
						echo (
							"select navid, type, url from myQuery where url = 'offense' and type = 2 and left(navid, 3) in (select navid from myQuery where type = 1 and url = 'football')"
						);
					}
				}).notToThrow();
				expect(myNewQuery.navid).tobe(20010);
				expect(myNewQuery.type).tobe(2);
				expect(myNewQuery.url).tobe('offense');
			});
		});
	}
}
