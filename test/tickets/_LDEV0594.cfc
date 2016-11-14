component extends="org.lucee.cfml.test.LuceeTestCase"{
	public function beforeAll(){
		myTestResults = structNew();
		myTestResults.result1 = "";
		myTestResults.result2 = "";
		myTestResults.result3 = "";
		myTestResults.result4 = "";
		myTestResults.result5 = "";
		myTestResults.result6 = "";
		myTestResults.result7 = "";
		myTestResults.result8 = "";
	}
	public function afterAll(){
		// writeDump(myTestResults);
	}
	public function run( testResults , testBox ) {
		describe( title="Testing cflock without a name(sleep + additional loop)", asyncAll=true, body=function() {
			it(title='First call', body=function( currentSpec ) {
				try {
					uri=createURI("LDEV0594/test1.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=1}
					);
					assertEquals("", left(result.filecontent.trim(), 83));
				} catch(any e) {
					myTestResults.result1 = e.Message;
				}
				if(myTestResults.result1 != "Java heap space")
					expect(myTestResults.result1).toBe("");
			});
			it(title='Second call', body=function( currentSpec ) {
				try {
					uri=createURI("LDEV0594/test1.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=1}
					);
					assertEquals("", left(result.filecontent.trim(), 83));
				} catch(any e) {
					myTestResults.result2 = e.Message;
				}
				if(myTestResults.result2 != "Java heap space")
					expect(myTestResults.result2).toBe("");
			});
		});

		describe( title="Testing cflock without a name(sleep only)", asyncAll=true, body=function() {
			it(title='First call', body=function( currentSpec ) {
				try {
					uri=createURI("LDEV0594/test1.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=2}
					);
					assertEquals("", left(result.filecontent.trim(), 83));
				} catch(any e) {
					myTestResults.result3 = e.Message;
				}
				if(myTestResults.result3 != "Java heap space")
					expect(myTestResults.result3).toBe("");
			});
			it(title='Second call', body=function( currentSpec ) {
				try {
					uri=createURI("LDEV0594/test1.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=2}
					);
					assertEquals("", left(result.filecontent.trim(), 83));
				} catch(any e) {
					myTestResults.result4 = e.Message;
				}
				if(myTestResults.result4 != "Java heap space")
					expect(myTestResults.result4).toBe("");
			});
		});

		describe( title="Testing cflock with a name(sleep + additional loop)", asyncAll=true, body=function() {
			it(title='First call', body=function( currentSpec ) {
				try{
					uri=createURI("LDEV0594/test2.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=1}
					);
					assertEquals("", left(result.filecontent.trim(), 83));
				} catch(any e) {
					myTestResults.result5 = e.Message;
				}
				if(myTestResults.result5 != "Java heap space")
					expect(myTestResults.result5).toBe("");
			});
			it(title='Second call', body=function( currentSpec ) {
				try{
					uri=createURI("LDEV0594/test2.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=1}
					);
					assertEquals("", left(result.filecontent.trim(), 83));
				} catch(any e) {
					myTestResults.result6 = e.Message;
				}
				if(myTestResults.result6 != "Java heap space")
					expect(myTestResults.result6).toBe("");
			});
		});

		describe( title="Testing cflock with a name(sleep only)", asyncAll=true, body=function() {
			it(title='First call', body=function( currentSpec ) {
				try{
					uri=createURI("LDEV0594/test2.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=2}
					);
					assertEquals("", left(result.filecontent.trim(), 83));
				} catch(any e) {
					myTestResults.result7 = e.Message;
				}
				if(myTestResults.result7 != "Java heap space")
					expect(myTestResults.result7).toBe("");
			});
			it(title='Second call', body=function( currentSpec ) {
				try{
					uri=createURI("LDEV0594/test2.cfm");
					local.result=_InternalRequest(
						template:uri,
						forms:{Scene=2}
					);
					assertEquals("", left(result.filecontent.trim(), 83));
				} catch(any e) {
					myTestResults.result8 = e.Message;
				}
				if(myTestResults.result8 != "Java heap space")
					expect(myTestResults.result8).toBe("");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}