component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		afterAll();
		variables.uri = createURI("LDEV_2902");
		directoryCreate("#variables.uri#/test");
	}

	function run( testResults, testBox ) { 
		describe( "testcase for LDEV-2902", function(){
			it( title = "Checking datasource configured with timezone",body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 1 }
				);
				expect(trim(result.filecontent)).toBe("America/Chicago");
			});

			it( title = "Checking datasource configured without timezone", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 2 }
				);
				expect(trim(result.filecontent)).toBe("key [TIMEZONE] doesn't exist");
			});

			it( title = "Checking datasource configured Empty timezone", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 3 }
				);
				
				var tz=createObject("java","java.util.TimeZone").getDefault();
				expect(trim(result.filecontent)).toBe(tz.id);
			});
		});
	}

	function afterAll(){
		variables.uri = createURI("LDEV_2902");
		if(directoryExists("#variables.uri#/test")){
			directoryDelete("#variables.uri#/test",true);
		}
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}   
