component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql,orm" {
	function run(){
		describe( title="Test suite for LDEV-233", skip=checkMySqlEnvVarsAvailable(), body=function(){
			it(title="Checking ORM with cftransaction", body=function(){
				var uri = createURI("LDEV0233/withTrans.cfm");
				var result = _InternalRequest(
					template:uri
				);
				if(isBoolean(result.fileContent.trim()))
					expect(result.fileContent.trim()).toBeTrue();
				else throw result.fileContent.trim()
			});

			it(title="Checking ORM without cftransaction", body=function(){
				var uri = createURI("LDEV0233/withoutTrans.cfm");
				var result = _InternalRequest(
					template:uri
				);
				if(isBoolean(result.fileContent.trim()))
					expect(result.fileContent.trim()).toBeTrue();
				else throw result.fileContent.trim()
			});
		});
	}

	// Private functions
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}

	private boolean function checkMySqlEnvVarsAvailable() {
		var mySQL= server.getDatasource("mysql");
		return structIsEmpty(mySQL);
	}
}