component extends="org.lucee.cfml.test.LuceeTestCase" labels="serialize" {
	function run( testResults , testBox ) {
		describe( "test case for LDEV-3998", function() {
			it( title = "include serialization error in exception when a java object can't be serialized ", body=function( currentSpec ) {
				var res = "test didn't throw an exception";
				try {
					var cfc = new LDEV3997.test();
					serialize(cfc);
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toInclude("can't serialize Object of type [ ");
			});
		});
	}
}