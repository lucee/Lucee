component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-486", function() {

			it(title="private function access with this scope", body = function( currentSpec ) {
				var cfc=new LDEV0486.test();
				var passed=false;
				try {
					cfc.testA();
					passed=true;
				}
				catch(local.e){}
				
				expect(passed).toBe(true);
			});

			it(title="private function access without any scope", body = function( currentSpec ) {
				var cfc=new LDEV0486.test();
				var passed=false;
				try {
					cfc.testB();
					passed=true;
				}
				catch(local.e){}
				
				expect(passed).toBe(true);
			});

			it(title="access private function directly via passed this scope", body = function( currentSpec ) {
				var cfc=new LDEV0486.test();
				var passed=false;
				try {
					cfc.getThis().testMethod();
					passed=true;
				}
				catch(local.e){}
				
				expect(passed).toBe(false);
			});




		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
