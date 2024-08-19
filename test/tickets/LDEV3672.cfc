component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

    function beforeAll(){
		variables.uri = createURI("LDEV3672");
	}

	function run( testResults , testBox ) { 

		describe( title='LDEV-3672' , body=function(){

			it( title='dumping a cfc errors with final' , body=function() {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV3672.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('success');
			});
		});
	}

    private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}