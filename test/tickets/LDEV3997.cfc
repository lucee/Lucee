component extends="org.lucee.cfml.test.LuceeTestCase" labels="serialize" skip=true{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-3997", function() {
			it( title = "serialize the component object which has a java object in variable", body=function( currentSpec ) {
				try {
					var cfc = new LDEV3997.test();
					serialize(cfc);
					var res = "success";
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toBe("success");
			});
		});
	}
}