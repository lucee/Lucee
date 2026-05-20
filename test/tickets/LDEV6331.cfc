component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" skip=true {

	function run( testResults, testBox ) {
		describe( "LDEV-6331 cache-backed sessions evict at TTL boundary on read-only patterns", function() {

			// Bug: IKHandlerCache.store() only calls cache.put() when hasChanges() is true.
			// hasChanges() flips on real CFML writes (session.foo=bar). Lucee's own
			// _lastvisit bump uses direct data0.put() and bypasses change detection,
			// so a "login then read-heavy" pattern never refreshes the cache TTL.
			//
			// Test mechanism (per storage + cluster combination):
			//   1. SET — single real session write, fires cache.put with TTL=2s.
			//   2. Sleep 2.5s — past both the cache TTL and the session timeout.
			//   3. purgeExpiredSessions — clears the in-memory copy via the force-flag
			//      path (storeUnusedStorageScope honouring scope.isExpired()).
			//   4. GET — no in-memory, cache evicted, fresh session created → session.user
			//      is lost.
			//
			// Matrix:
			//   - RAM cache vs Redis (Redis skipped if test service unavailable)
			//   - sessionCluster=false vs sessionCluster=true
			//
			// Before fix: assertions FAIL — session.user is "(undefined)".
			// After fix:  assertions PASS — touch path refreshes the cache TTL before
			//             the boundary so the cache still has the entry.

			it( title="RAM cache, sessionCluster=false: session preserved across cache-TTL boundary", body=function( currentSpec ) {
				testCacheBackedSessionEviction( storage: "ram", sessionCluster: false );
			});

			it( title="RAM cache, sessionCluster=true: session preserved across cache-TTL boundary", body=function( currentSpec ) {
				testCacheBackedSessionEviction( storage: "ram", sessionCluster: true );
			});

			it( title="Redis, sessionCluster=false: session preserved across cache-TTL boundary", skip=skipRedis(), body=function( currentSpec ) {
				testCacheBackedSessionEviction( storage: "redis", sessionCluster: false );
			});

			it( title="Redis, sessionCluster=true: session preserved across cache-TTL boundary", skip=skipRedis(), body=function( currentSpec ) {
				testCacheBackedSessionEviction( storage: "redis", sessionCluster: true );
			});
		});
	}

	private function testCacheBackedSessionEviction( required string storage, required boolean sessionCluster ) {
		var uri = createURI( "LDEV6331" );
		var label = "#arguments.storage# cluster=#arguments.sessionCluster#";
		var urlArgs = {
			user: "zac",
			sessionStorage: arguments.storage,
			sessionCluster: arguments.sessionCluster
		};

		// 1. SET — the one and only real session write
		var setResp = _InternalRequest( template: "#uri#/set.cfm", url: urlArgs );
		expect( setResp.fileContent ).toBeJson();
		var setData = deserializeJSON( setResp.fileContent );
		expect( setData.sessionUser ).toBe( "zac" );

		var cookies = {
			cfid: setResp.session.cfid,
			cftoken: setResp.session.cftoken
		};
		var appName = setData.applicationName;

		expect( getSessionCount( appName ) ).toBe( 1, "[#label#] session should be in memory after SET" );

		// 2. Sleep past cache TTL AND session timeout (both 2s)
		sleep( 2500 );

		// 3. Force purge — relies on clearUnused honouring force flag for
		//    cache-backed sessions (the storeUnusedStorageScope fix).
		admin
			action="purgeExpiredSessions"
			type="server"
			password="#request.SERVERADMINPASSWORD#";

		expect( getSessionCount( appName ) ).toBe( 0, "[#label#] in-memory session should be purged after sleep > sessionTimeout + purgeExpiredSessions" );

		// 4. GET — no in-memory, cache evicted, must create fresh session
		var getResp = _InternalRequest(
			template: "#uri#/get.cfm",
			url: {
				sessionStorage: arguments.storage,
				sessionCluster: arguments.sessionCluster
			},
			cookies: cookies
		);
		expect( getResp.fileContent ).toBeJson();
		var getData = deserializeJSON( getResp.fileContent );

		expect( getData.sessionUser ).toBe(
			"zac",
			"BUG LDEV-6331 [#label#]: cache-backed session evicted at cache TTL boundary. "
			& "Expected session.user='zac' but got '#getData.sessionUser#'. "
			& "The fix should refresh the cache TTL on read-only requests so the entry survives."
		);
	}

	private numeric function getSessionCount( applicationName ) {
		var sess = getPageContext().getCFMLFactory().getScopeContext().getAllCFSessionScopes();
		if ( structKeyExists( sess, arguments.applicationName ) )
			return len( sess[ arguments.applicationName ] );
		else
			return 0;
	}

	private boolean function skipRedis() {
		var redis = server.getTestService( "redis" );
		return isNull( redis ) || structCount( redis ) == 0;
	}

	private string function createURI( required string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}
}
