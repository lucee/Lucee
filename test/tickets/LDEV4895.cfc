component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {
	
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4895 - invalid conditional operator", function() {
			it( title="check syntax double ternary", body=function( currentSpec ) {
				expect( function(){
					internalRequest(
						template="#createURI('LDEV4985')#/ldev4895.cfs"
					).notToThrow();
				});

				// odd, same code in .cfs only crashes here
				// var a = false ? true ? 1 : 2;
			});
		}); 
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}