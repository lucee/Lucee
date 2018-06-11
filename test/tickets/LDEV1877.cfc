component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.base = GetDirectoryFromPath(GetCurrentTemplatePath());
		if(directoryExists(variables.base&"LDEV1877"))
			directorydelete(variables.base&'LDEV1877', true);
		directoryCreate(variables.base&'LDEV1877');
	}

	function afterAll() {
		variables.base = GetDirectoryFromPath(GetCurrentTemplatePath());
		if(directoryExists(variables.base&"LDEV1877"))
			directorydelete(variables.base&'LDEV1877', true);
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV-1877", function() {
			it(title = "checking the file status before fileClose()", body = function( currentSpec ) {
				var path = variables.base&"/LDEV1877/sample.txt";
				var result=fileopen(path,'write');
				fileWrite(result,'I love lucee');
				expect(result.status).tobe('open');
				fileclose(result);
			});

			it(title = "checking the file status after fileClose()", body = function( currentSpec ) {
				var path = variables.base&"/LDEV1877/sample.txt";
				var result=fileopen(path,'write');
				fileWrite(result,'I love lucee');
				fileclose(result);
				expect(result.status).tobe('close');
			});
		});
	}
}