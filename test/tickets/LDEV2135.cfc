component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-2135 using memory", function() {

			//beforeEach(function (currentSpec, data){ _beforeEach(currentSpec, data); });

			it( title='thread looses session variables - sessionCluster=false', body=function( currentSpec ) {
				test( {sessionCluster: false, sessionStorage: "memory"} );
			});

			it( title='thread looses session variables - sessionCluster=true', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "memory"} );
			});
		});

		describe( title="Test suite for LDEV-2135 using redis", skip=skipRedis(), body=function() {
			
			//beforeEach(function (currentSpec, data){ _beforeEach(currentSpec, data); });

			it( title='thread looses session variables - redis- sessionCluster=false', body=function( currentSpec ) {
				test( {sessionCluster: false, sessionStorage: "redis"} );
			});

			it( title='thread looses session variables - redis - sessionCluster=true', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "redis"} );
			});

			it( title='thread looses session topLevel variables - redis - sessionCluster=true', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "redis"}, "TopLevel" );
			});
		});

		describe( title="Test suite for LDEV-2135 using memcached", skip=skipMemcached(), body=function() {

			//beforeEach(function (currentSpec, data){ _beforeEach(currentSpec, data); });

			it( title='thread looses session variables - memcached -sessionCluster=false', body=function( currentSpec ) {
				test( {sessionCluster: false, sessionStorage: "memcached"} );
			});

			it( title='thread looses session variables - memcached -sessionCluster=true', body=function( currentSpec ) {
				test( {sessionCluster: true, sessionStorage: "memcached"} );
			});
		});
	}

	private function test( args, string template="" ){
		var uri = createURI( "LDEV2135" );
		var first = _InternalRequest(
			template : "#uri#/cfml-session/testThreadSession#template#.cfm",
			url: args
		);
		systemOutput(args, true);
		checkSess( first.fileContent );

		var cookies = {
			cfid: first.session.cfid,
			cftoken: first.session.cftoken
		};
		systemOutput("-- before secondRequest.cfm", true); 
		var second = _InternalRequest(
			template : "#uri#/cfml-session/secondRequest#template#.cfm",
			url: args,
			cookies: cookies
		);
		checkSess( second.fileContent ); // this fails with sessionCluster=true as the sessions can be out of sync, even on a single "cluster"

	}

	private function checkSess( fileContent ){
		expect( fileContent).toBeJson();
		// systemOutput( fileContent, true );
		var s = deserializeJSON( fileContent );
		expect( s ).toHaveKey( "start" );
		expect( s ).toHaveKey( "threads" );
		expect( s.threads ).toHaveLength( 5 );
		expect( s ).toHaveKey( "afterJoin" );
	}
	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}

	private function skipRedis(){
		return (structCount(server.getTestService( "redis" )) eq 0);
	}

	private function skipMemcached(){
		return (structCount(server.getTestService( "memcached" )) eq 0);
	}

	private function _beforeEach(currentSpec, data){
		systemOutput("", true);
		systemOutput(currentspec, true);
	}
}