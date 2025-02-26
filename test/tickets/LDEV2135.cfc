component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-2135 using memory", function() {

			it( title='thread looses session variables - sessionCluster=false', body=function( currentSpec ) {
				test( {sessionCluster: false, sessionStorage: "memory"} );
			});

			it( title='thread looses session variables - sessionCluster=true', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "memory"} );
			});
		});

		describe( title="Test suite for LDEV-2135 using redis", skip=skipRedis(), body=function() {

			it( title='thread looses session variables - redis - sessionCluster=false', body=function( currentSpec ) {
				test( {sessionCluster: false, sessionStorage: "redis"} );
			});

			// sessionCommit/sessionTouch is required here as Lucee only detects top-level session changes,
			// not modifications to nested data like session.foo.bar

			it( title='thread looses session variables - redis - sessionCluster=true - sessionCommit', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "redis", sessionCommit: true} );
			});

			it( title='thread looses session variables - redis - sessionCluster=true - sessionTouch', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "redis", sessionTouch: true} );
			});

			// TopLevel test doesn't need sessionCommit/sessionTouch because session.topLevel is a top-level change
			it( title='thread looses session topLevel variables - redis - sessionCluster=true', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "redis"}, "TopLevel" );
			});

			// Test that sessionCommit doesn't cause double-write - nested changes after sessionCommit should be lost
			// unless sessionTouch is called again
			it( title='sessionCommit does not double-write - redis - sessionCluster=true', body=function( currentSpec ) {
				testNoDoubleWrite( {sessionCluster: true, sessionStorage: "redis"} );
			});
		});

		describe( title="Test suite for LDEV-2135 using memcached", skip=skipMemcached(), body=function() {

			it( title='thread looses session variables - memcached - sessionCluster=false', body=function( currentSpec ) {
				test( {sessionCluster: false, sessionStorage: "memcached"} );
			});

			it( title='thread looses session variables - memcached - sessionCluster=true - sessionCommit', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "memcached", sessionCommit: true} );
			});

			it( title='thread looses session variables - memcached - sessionCluster=true - sessionTouch', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "memcached", sessionTouch: true} );
			});
		});
	}

	private function test( args, string template="" ){
		var uri = createURI( "LDEV2135" );
		var first = _InternalRequest(
			template : "#uri#/cfml-session/testThreadSession#template#.cfm",
			url: args
		);
		systemOutput( args, true );
		checkSess( first.fileContent );

		var cookies = {
			cfid: first.session.cfid,
			cftoken: first.session.cftoken
		};
		var second = _InternalRequest(
			template : "#uri#/cfml-session/secondRequest#template#.cfm",
			url: args,
			cookies: cookies
		);
		checkSess( second.fileContent );
	}

	private function checkSess( fileContent ){
		expect( fileContent ).toBeJson();
		var s = deserializeJSON( fileContent );
		expect( s ).toHaveKey( "start" );
		expect( s ).toHaveKey( "threads" );
		expect( s.threads ).toHaveLength( 5 );
		expect( s ).toHaveKey( "afterJoin" );
	}

	private string function createURI( string calledName, boolean contract=true ){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast( base, "\/" )#";
		return baseURI & "/" & calledName;
	}

	private function skipRedis(){
		return ( structCount( server.getTestService( "redis" ) ) eq 0 );
	}

	private function skipMemcached(){
		return ( structCount( server.getTestService( "memcached" ) ) eq 0 );
	}

	private function testNoDoubleWrite( args ){
		var uri = createURI( "LDEV2135" );

		// First request: set nested value, sessionCommit, then change nested value again
		var first = _InternalRequest(
			template : "#uri#/cfml-session/testNoDoubleWrite.cfm",
			url: args
		);

		expect( first.fileContent ).toBeJson();
		var firstData = deserializeJSON( first.fileContent );
		expect( firstData.nestedValue ).toBe( "committed" );

		var cookies = {
			cfid: first.session.cfid,
			cftoken: first.session.cftoken
		};

		// Clear the session from memory (not from Redis) to force reload on next request
		_InternalRequest(
			template : "#uri#/cfml-session/stopApp.cfm",
			url: args,
			cookies: cookies
		);

		// Second request: check the nested value - should be "committed" not "changedAfterCommit"
		// because the change after sessionCommit was nested and hasChanges was false
		var second = _InternalRequest(
			template : "#uri#/cfml-session/testNoDoubleWriteCheck.cfm",
			url: args,
			cookies: cookies
		);

		expect( second.fileContent ).toBeJson();
		var secondData = deserializeJSON( second.fileContent );
		// If setClean() works, the nested change after sessionCommit should NOT be persisted
		expect( secondData.nestedValue ).toBe( "committed" );
	}
}
