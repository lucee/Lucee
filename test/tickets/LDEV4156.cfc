component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-4156", function() {
			it( title="Checking new java(className) syntax for create java object", body = function( currentSpec ) {
				try {
					var res = new java("java.lang.String").init("new java(className) syntax works as alias of createObjec()");
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toBe("new java(className) syntax works as alias of createObjec()");
			});
			it( title="Checking new Component(cfcPath) syntax for create component object", body = function( currentSpec ) {
				try {
					var res = new Component("LDEV4156").getStr();
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toBe("new component(cfcPath) syntax works as alias of createObjec()");
			});
		});
	}

	public string function getStr() {
		return "new component(cfcPath) syntax works as alias of createObjec()";
	}
	
}