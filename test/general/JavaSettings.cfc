component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.origSerSettings =  getApplicationSettings().serialization;
	}

	function afterAll(){
		application action="update"
			SerializationSettings = variables.origSerSettings;
	}

	function run( testResults , testBox ) {
		describe( "test suite for this.javaSettings", function() {

			it(title="checking Classic Jar", body=function(){
				var uri=createURI("javaSettings/classic/index.cfm");
				var res=_InternalRequest(addToken:true,template:uri);
				var data=deserializeJson(res.fileContent);
				
				expect(data.bundle).toBe(false);
			});

			it(title="checking OSGi 2 Bundle", body=function(){
				var uri=createURI("javaSettings/osgi2/index.cfm");
				var res=_InternalRequest(addToken:true,template:uri);
				var data=deserializeJson(res.fileContent);
				
				expect(data.bundle.name).toBe("lucee.mockup2");
				expect(data.bundle.version).toBe("1.0.0.0");
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
