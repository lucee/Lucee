component extends = "org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.base = GetDirectoryFromPath(GetCurrentTemplatePath());
		if(!directoryExists(variables.base&"LDEV2533")){
			directoryCreate(variables.base&'LDEV2533');
		}
	}
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-2533", function() {
			it(title = "Status showing open - when using fileClose()", body = function( currentSpec ) {
				var path = variables.base&"/LDEV2533/sample.txt";
				var result=fileopen(path,'write');
				fileWrite(result,'I love lucee');
				expect(result.status).tobe('open');
				fileclose(result);
				expect(result.status).tobe('close');
			});
		});
	}
	function afterAll(){
		if(directoryExists(variables.base&"LDEV2533")){
			directorydelete(variables.base&'LDEV2533', true);
		}
	}
}