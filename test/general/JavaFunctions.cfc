component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){	
	}

	function afterAll(){
	}

	function run( testResults , testBox ) {
		describe( "test suite for java functions", function() {

			it(title="checking echoint", body=function(){
				var uri=createURI("javaFunctions/echoInt.cfm");
				var res=_InternalRequest(addToken:true,template:uri);
				var data=res.fileContent;
				
				expect(data).toBe("8");
			});

			it(title="checking toString", body=function(){
				var uri=createURI("javaFunctions/toString.cfm");
				var res=_InternalRequest(addToken:true,template:uri);
				var data=res.fileContent;
				
				expect(data).toBe("ab");
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
