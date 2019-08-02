component extends = "org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2398");
		if(!directoryExists(uri)){
			directoryCreate(uri);
		}
		fr = "<";
		br = ">";
		fileWrite(uri&'\test.cfm',"#fr#cfthrow message = 'boom'#br#");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV2398", function(){
			it( title = "HTTP handle the exceptions thrown from the request file", body = function( currentSpec ){
				cfhttp( method = "POST", url = "#CGI.SERVER_NAME##variables.uri#/test.cfm" result = "res" ){}
				expect(res.status_code eq 500).tobe(true);
			});

			it( title = "_internalRequest doesn't handle exceptions thrown from the internal request", body = function( currentSpec ){
					local.result = _InternalRequest(
					template : "#uri#\test.cfm"
				);
				expect(res.status_code eq 500).tobe(true);
			});
		});
	}
	
	function afterAll(){
		if(directoryExists(uri)){
			directoryDelete(uri,true);
		}
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
