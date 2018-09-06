component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1969", body=function(){
			it(title="cannot address a struct inside a query", body=function(){
				qry=query(a:[{b:{c:123}}]);
				expect(qry.a.b.c).toBe(123);
			});
		});
	}
}
