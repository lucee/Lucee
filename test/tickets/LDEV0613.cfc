component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll() {

	}
	function run( testResults , testBox ) {
		describe( 'Running hql query with script' , function() {
			it('With OrmExecuteQuery',  function( currentSpec ) {
				http method="get" result="local.result" url="#createURL("LDEV0613/index.cfm?Scene=1")#" addtoken="false";
				assertEquals("",result.filecontent.trim());
			});
		});

		describe( 'Running hql query with tag' , function() {
			it('With dbtype hql',  function( currentSpec ) {
				http method="get" result="local.result" url="#createURL("LDEV0613/index.cfm?Scene=2")#" addtoken="false";
				assertEquals("",left(result.filecontent.trim(), 100));
				// assertEquals("",1);
			});
		});
	}

	private string function createURL(string calledName){
		var baseURL="http://#cgi.HTTP_HOST##getDirectoryFromPath(contractPath(getCurrenttemplatepath()))#";
		return baseURL&""&calledName;
	}
}