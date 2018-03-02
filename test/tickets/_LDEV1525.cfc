component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1525");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1525", function() {
			it(title = "Checking QoQ while missing column", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				var replaceName = replace(local.result.filecontent,"org.hsqldb.Expression","name");
				var changeName = listfirst(replaceName,"@") & " " & "in" & listdeleteat(listlast(replaceName,"@"),"1","in");
				expect(local.result.filecontent.trim()).toBe(changeName);
			});

		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}