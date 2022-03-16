component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true"{
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-3908", function() {
			it(title="Check importDefinition of the component", body=function( currentSpec ) {
				expect(new LDEV3912.app1.testApp1().accessImportInApp1()).toBe("from app1 import cfc");
				});
			it(title="Check importDefinition of the component with same path and name", body=function( currentSpec ) {
				try {
					var res = new LDEV3912.app2.testApp2().accessImportInApp2();
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toBe("from app2 import cfc");
			});
		});
	}
}