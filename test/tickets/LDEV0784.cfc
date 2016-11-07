component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Checking CSRFVerifyToken()", function() {
			it('With invalid data',  function( currentSpec ) {
				uri=createURI("LDEV0784/app1/index.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				assertEquals("false",left(result.filecontent.trim(), 100));
			});
			it('With valid data(without key)',  function( currentSpec ) {
				uri=createURI("LDEV0784/app1/index.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				assertEquals("true",left(result.filecontent.trim(), 100));
			});

			it('With valid data(with key)',  function( currentSpec ) {
				uri=createURI("LDEV0784/app1/index.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene=3}
				);
				assertEquals("true",left(result.filecontent.trim(), 100));
			});
		});

		describe( "this.SessionCluster = false;", function() {
			it('forceNew=true',  function( currentSpec ) {
				uri=createURI("LDEV0784/app1/test.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				assertEquals("false",left(result.filecontent.trim(), 100));
			});
			it('forceNew=false',  function( currentSpec ) {
				uri=createURI("LDEV0784/app1/test.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				assertEquals("true",left(result.filecontent.trim(), 100));
			});
		});

		describe( "this.SessionCluster = true;", function() {
			it('forceNew=true',  function( currentSpec ) {
				uri=createURI("LDEV0784/app2/test.cfm");
				local.result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				assertEquals("false",left(result.filecontent.trim(), 100));
			});
			it('forceNew=false',  function( currentSpec ) {
				uri=createURI("LDEV0784/app2/test.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				assertEquals("true",left(result.filecontent.trim(), 100));
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}