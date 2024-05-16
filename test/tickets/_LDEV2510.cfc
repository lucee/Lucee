component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){

		uri = createURI("LDEV2510");
		variables.pathOne = uri
		variables.pathTwo = uri&"\custompath";
		if(!directoryExists(pathOne)){
			directorycreate(pathOne)
		}
		if(!directoryExists(pathTwo)){
			directorycreate(pathTwo)
		}
		fr = "<";
		br = ">";
		filewrite(pathOne&'\test.cfm',"#fr#cfmodule name = 'custom'#br#");
		filewrite(pathOne&'\test1.cfm',"#fr#cfmodule name = 'custompath.custom'#br#");
		filewrite(pathOne&'\Application.cfc',"#fr#cfcomponent#br##chr(13)##fr#cfset this.customtagpaths = '#pathOne#'#br##chr(13)##fr#/cfcomponent#br#");
		filewrite(pathTwo&'\custom.cfm',"#fr#cfoutput#br##asc('S')##fr#/cfoutput#br#");

	}
	
	function run( testResults , testBox ) {

		describe( "test suite for LDEV2510", function() {
			it(title = "cfmodule considered a subfolder of custom tag path when using dot notation - childpath", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#pathOne#"&"\test1.cfm"
				);
				expect(83).toBe(result.fileContent);
			});

			it(title = " cfmodule doesn't consider a subfolder of custom tag path", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#pathOne#"&"\test.cfm"
				);
				expect(83).toBe(result.fileContent);
			});
		});
	}

	function afterAll(){
		if(directoryExists(pathOne)) directoryDelete(pathOne,true);
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}