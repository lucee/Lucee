component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm"{
	function beforeAll() {

	}
	function run( testResults , testBox ) {
		describe( 'Running hql query with script' , function() {
			it( title='With OrmExecuteQuery', skip=noOrm(), body=function( currentSpec ) {
				local.uri=createURI("LDEV0613/index.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				assertEquals("",result.filecontent.trim());
			});
		});

		describe( 'Running hql query with tag' , function() {
			it( title='With dbtype hql', skip=noOrm(), body=function( currentSpec ) {
				local.uri=createURI("LDEV0613/index.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				assertEquals("",left(result.filecontent.trim(), 100));
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function noOrm() {
		return ( structCount( server.getTestService("orm") ) eq 0 );
	}
}