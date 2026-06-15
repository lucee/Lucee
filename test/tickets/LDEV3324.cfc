component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {
	
	function run( testResults , testBox ) {
		describe( "LDEV3324 - CSRF tokens are preserved with sessionRotate()", function() {

			it( title='cfml session', body=function( currentSpec ) {
				var uri = createURI("LDEV3324");
				var cfmlSessionId = _InternalRequest(
					template : "#uri#/cfml_session_rotate/test_sessionRotate_csrf.cfm"
				);
				expect( cfmlSessionId.fileContent ).toInclude( "all good" );
			});

			it( title='jee session', body=function( currentSpec ) {
				var uri = createURI("LDEV3324");
				var j2eeSessionId = _InternalRequest(
					template : "#uri#/jee_session_rotate/test_sessionRotate_csrf.cfm"
				);
				expect( j2eeSessionId.fileContent ).toInclude( "all good" );
			});

			it( title='cfml session with cache storage (LDEV-6412)', body=function( currentSpec ) {
				var uri = createURI("LDEV3324");
				var resp = _InternalRequest(
					template : "#uri#/cfml_session_rotate_cache/test_sessionRotate_csrf.cfm"
				);
				expect( resp.fileContent ).toInclude( "all good" );
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}