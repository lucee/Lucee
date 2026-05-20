component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	// Bug: IKHandlerCache.store() / IKHandlerDatasource.store() only persist when hasChanges()
	// is true. hasChanges() flips on real CFML writes (session.foo=bar). Lucee's own _lastvisit
	// bump uses direct data0.put() and bypasses change detection, so a "login then read-heavy"
	// pattern never refreshes the storage entry's TTL / expires column.
	//
	// Fix: isStale() branch in hasChanges() returns true after wall-clock since last store
	// exceeds keepAlive (defaults to sessionTimeout/2).
	//
	// Test flow per storage backend:
	//   t=0     SET — single real session write, persists with TTL=4s
	//   t=2.5s  mid-TTL GET — past keepAlive (2s), fix triggers refresh
	//   t=5s    final GET — past original TTL (4s), within refreshed TTL (~6.5s)
	//
	// Without fix: final GET finds storage entry gone (sessionInStorage=false).
	// With fix:    final GET finds storage entry still alive.

	function run( testResults, testBox ) {

		describe( "LDEV-6331 RAM cache-backed sessions: storage entry refreshed before TTL boundary", function() {
			it( title="sessionCluster=false", body=function() {
				testStorageRefresh( storage: "ram", sessionCluster: false );
			});
			it( title="sessionCluster=true", body=function() {
				testStorageRefresh( storage: "ram", sessionCluster: true );
			});
		});

		describe( "LDEV-6331 Redis cache-backed sessions: storage entry refreshed before TTL boundary", function() {
			it( title="sessionCluster=false", skip=skipRedis(), body=function() {
				testStorageRefresh( storage: "redis", sessionCluster: false );
			});
			it( title="sessionCluster=true", skip=skipRedis(), body=function() {
				testStorageRefresh( storage: "redis", sessionCluster: true );
			});
		});

		describe( "LDEV-6331 + LDEV-4670 Datasource (MySQL) sessions: cf_session_data.expires refreshed", function() {
			it( title="sessionCluster=false", skip=skipMysql(), body=function() {
				testStorageRefresh( storage: "datasource", sessionCluster: false );
			});
			it( title="sessionCluster=true", skip=skipMysql(), body=function() {
				testStorageRefresh( storage: "datasource", sessionCluster: true );
			});
		});
	}

	private function testStorageRefresh( required string storage, required boolean sessionCluster ) {
		var uri = createURI( "LDEV6331" );
		var label = "#arguments.storage# cluster=#arguments.sessionCluster#";
		var urlArgs = {
			user: "zac",
			sessionStorage: arguments.storage,
			sessionCluster: arguments.sessionCluster
		};

		// t=0 — SET, cache.put / DB insert fires with TTL/expires at t+4s
		var setResp = _InternalRequest( template: "#uri#/set.cfm", url: urlArgs );
		expect( setResp.fileContent ).toBeJson();
		expect( deserializeJSON( setResp.fileContent ).sessionUser ).toBe( "zac" );

		var cookies = {
			cfid: setResp.session.cfid,
			cftoken: setResp.session.cftoken
		};
		var getUrlArgs = {
			sessionStorage: arguments.storage,
			sessionCluster: arguments.sessionCluster
		};

		// t~2.5s — mid-TTL GET, past keepAlive=2s, fix's isStale() triggers a refresh write at end of request
		sleep( 2500 );
		_InternalRequest( template: "#uri#/get.cfm", url: getUrlArgs, cookies: cookies );

		// t~5s — past original TTL=4s, within refreshed TTL (~2.5+4=6.5s)
		sleep( 2500 );
		var finalResp = _InternalRequest( template: "#uri#/get.cfm", url: getUrlArgs, cookies: cookies );
		var finalData = deserializeJSON( finalResp.fileContent );
		expect( finalData.sessionInStorage ).toBeTrue(
			"BUG LDEV-6331 [#label#]: persisted session entry evicted at original TTL boundary "
			& "despite a mid-TTL read that should have refreshed it."
		);
	}

	private boolean function skipRedis() {
		var redis = server.getTestService( "redis" );
		return isNull( redis ) || structCount( redis ) == 0;
	}

	private boolean function skipMysql() {
		var mysql = server.getDatasource( "mysql" );
		return isEmpty( mysql );
	}

	private string function createURI( required string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}
}
