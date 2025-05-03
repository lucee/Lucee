component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Checking CSRFVerifyToken()", function() {
			it('With invalid data',  function( currentSpec ) {
				var uri=createURI("LDEV0784/app1/index.cfm");
				var result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				assertEquals("false",left(result.filecontent.trim(), 100));
			});
			it('With valid data(without key)',  function( currentSpec ) {
				var uri=createURI("LDEV0784/app1/index.cfm");
				var result=_InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				assertEquals("true",left(result.filecontent.trim(), 100));
			});

			it('With valid data(with key)',  function( currentSpec ) {
				var uri=createURI("LDEV0784/app1/index.cfm");
				var result=_InternalRequest(
					template:uri,
					forms:{Scene=3}
				);
				assertEquals("true",left(result.filecontent.trim(), 100));
			});
		});

		describe( "this.SessionCluster = false;", function() {
			it('forceNew=true',  function( currentSpec ) {
				var uri=createURI("LDEV0784/app1/test.cfm");
				var result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				assertEquals("false",left(result.filecontent.trim(), 100));
			});
			it('forceNew=false',  function( currentSpec ) {
				var uri=createURI("LDEV0784/app1/test.cfm");
				var result=_InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				assertEquals("true",left(result.filecontent.trim(), 100));
			});
		});

		describe( "this.SessionCluster = true;", function() {
			it('forceNew=true',  function( currentSpec ) {
				var uri=createURI("LDEV0784/app2/test.cfm");
				var result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				assertEquals("false",left(result.filecontent.trim(), 100));
			});
			it('forceNew=false',  function( currentSpec ) {
				var uri=createURI("LDEV0784/app2/test.cfm");
				var result=_InternalRequest(
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