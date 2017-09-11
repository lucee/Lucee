component extends="org.lucee.cfml.test.LuceeTestCase"{

	function afterAll(){
		base =  "#getDirectoryFromPath(getCurrentTemplatePath())#LDEV1487\";
		if(fileExists("#base#_demo.cfc")){
			filecopy("#base#_demo.cfc" , "#base#demo.cfc");
			fileDelete("#base#_demo.cfc");
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1487",  body=function() {
			it(title="checking createobject(), called via non existing component", body = function( currentSpec ) {
				var uri = createURI('LDEV1487');
				var result = _InternalRequest(
					template:"#uri#/index.cfm"
				);
				var result2 = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(result2.filecontent.trim()).toBe('true')
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
