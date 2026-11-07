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

			// TODO LDEV-6627: the OSGi bundle specs are skipped until the OSGi extension exists, they need the OSGi service to install and load the mockup bundles. Enable them (remove skip=true) as soon as the OSGi extension is available.
			it(title="checking OSGi Bundle", skip=true, body=function(){
				var uri=createURI("javaSettings/osgi/index.cfm");
				var res=_InternalRequest(addToken:true,template:uri);
				var data=deserializeJson(res.fileContent);
				
				expect(data.bundle1.name).toBe("lucee.mockup");
				expect(data.bundle1.version).toBe("1.0.0.0");

				expect(data.bundle2.name).toBe("lucee.mockup");
				expect(data.bundle2.version).toBe("1.0.0.0");
			});

			it(title="checking Classic Jar", body=function(){
				var uri=createURI("javaSettings/classic/index.cfm");
				var res=_InternalRequest(addToken:true,template:uri);
				var data=deserializeJson(res.fileContent);
				
				expect(data.bundle).toBe(false);
			});


			it(title="checking OSGi Update Bundle", skip=true, body=function(){
				var uri=createURI("javaSettings/osgiupdate/index.cfm");
				var res=_InternalRequest(addToken:true,template:uri);
				var data=deserializeJson(res.fileContent);
				
				expect(data.bundle1.name).toBe("lucee.mockup");
				expect(data.bundle1.version).toBe("1.0.0.0");

				expect(data.bundle2.name).toBe("lucee.mockup");
				expect(data.bundle2.version).toBe("1.0.0.0");
			});


			it(title="checking OSGi 2 Bundle", skip=true, body=function(){
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
