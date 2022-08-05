component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm"{
	function beforeAll() {

	}
	function run( testResults , testBox ) {
		describe( 'Running hql query' , function() {
			it('With OrmExecuteQuery',  function( currentSpec ) {
				uri=createURI("LDEV1214/index.cfm");
				local.result=_InternalRequest(
					template:uri
				);
				assertEquals("Random Entity",unwrap(result.filecontent));
			});
		});
	}

	private function unwrap(String str) {
		str = str.trim();
		if((left(str,1)==chr(8220) || left(str,1)=='"') && (right(str,1)=='"' || right(str,1)==chr(8221)))
			str=mid(str,2,len(str)-2);
		else if(left(str,1)=="'" && right(str,1)=="'")
			str=mid(str,2,len(str)-2);
		return str;
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}