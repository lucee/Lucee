component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	// Bug: IKHandlerCache.store() / IKHandlerDatasource.store() only persist when hasChanges()
	// is true. hasChanges() flips on real CFML writes (session.foo=bar). Lucee's own _lastvisit
	// bump uses direct data0.put() and bypasses change detection, so a "login then read-heavy"
	// pattern never refreshes the storage entry's TTL / expires column.
	//
	// Fix: isStale() branch in hasChanges() returns true after wall-clock since last store
	// exceeds sessionCommitInterval (defaults to sessionTimeout/2).
	//
	// Test flow per storage backend (TTL = variables.ttlSeconds, commitInterval = TTL/2):
	//   t=0           SET — single real session write, persists with TTL
	//   t=TTL*0.7s    mid-TTL GET — past commitInterval, before TTL, fix triggers refresh
	//   t=TTL*1.3s    final GET — past original TTL, within refreshed TTL (~TTL*1.7s)
	//
	// Without fix: final GET finds storage entry gone (sessionInStorage=false).
	// With fix:    final GET finds storage entry still alive.
	// Redis EXPIRE is seconds-precision, so 2s is the practical floor — bump if flaky on slow CI.
	variables.ttlSeconds = 2;

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

		describe( "LDEV-6331 opt-out: this.sessionCommitInterval = createTimespan(0,0,0,0) disables periodic refresh", function() {
			it( title="zero sessionCommitInterval — storage entry evicts at original TTL (pre-fix behaviour preserved)", body=function() {
				testStorageRefresh( storage: "ram", sessionCluster: false, sessionCommitInterval: 0, expectInStorage: false );
			});
		});

		describe( "LDEV-6331 getApplicationSettings() exposes sessionCommitInterval", function() {
			it( title="absent from struct when not set (default applies)", body=function() {
				var uri = createURI( "LDEV6331" );
				var resp = _InternalRequest( template: "#uri#/settings.cfm", url: { sessionStorage: "ram", sessionCluster: false, ttlSeconds: variables.ttlSeconds } );
				var data = deserializeJSON( resp.fileContent );
				expect( data.hasSessionCommitInterval ).toBeFalse(
					"getApplicationSettings() should not contain sessionCommitInterval when unset — surfaces the default-applies semantic."
				);
			});

			it( title="present with explicit value when set", body=function() {
				var uri = createURI( "LDEV6331" );
				var resp = _InternalRequest( template: "#uri#/settings.cfm", url: { sessionStorage: "ram", sessionCluster: false, ttlSeconds: variables.ttlSeconds, sessionCommitInterval: 5 } );
				var data = deserializeJSON( resp.fileContent );
				expect( data.hasSessionCommitInterval ).toBeTrue( "getApplicationSettings() should contain sessionCommitInterval when set." );
				expect( data.sessionCommitInterval ).toBe( 5000, "sessionCommitInterval should be exposed as 5000ms TimeSpan." );
			});
		});
	}

	private function testStorageRefresh( required string storage, required boolean sessionCluster, string sessionCommitInterval="", boolean expectInStorage=true ) {
		var uri = createURI( "LDEV6331" );
		var label = "#arguments.storage# cluster=#arguments.sessionCluster#" & ( len( arguments.sessionCommitInterval ) ? " commitInterval=#arguments.sessionCommitInterval#" : "" );
		var userValue = "user-" & createUUID();
		var ttlMs = variables.ttlSeconds * 1000;
		var midSleep = round( ttlMs * 0.7 );
		var finalSleep = round( ttlMs * 0.6 );
		var urlArgs = {
			user: userValue,
			sessionStorage: arguments.storage,
			sessionCluster: arguments.sessionCluster,
			ttlSeconds: variables.ttlSeconds,
			sessionCommitInterval: arguments.sessionCommitInterval
		};

		// t=0 — SET, cache.put / DB insert fires with TTL/expires at t+TTL
		var setResp = _InternalRequest( template: "#uri#/set.cfm", url: urlArgs );
		expect( setResp.fileContent ).toBeJson();
		expect( deserializeJSON( setResp.fileContent ).sessionUser ).toBe( userValue );

		var cookies = {
			cfid: setResp.session.cfid,
			cftoken: setResp.session.cftoken
		};
		var getUrlArgs = {
			sessionStorage: arguments.storage,
			sessionCluster: arguments.sessionCluster,
			ttlSeconds: variables.ttlSeconds,
			sessionCommitInterval: arguments.sessionCommitInterval
		};

		// t~TTL*0.7s — mid-TTL GET, past commitInterval=TTL/2, fix's isStale() triggers a refresh write at end of request
		sleep( midSleep );
		_InternalRequest( template: "#uri#/get.cfm", url: getUrlArgs, cookies: cookies );

		// t~TTL*1.3s — past original TTL, within refreshed TTL (~TTL*1.7s)
		sleep( finalSleep );
		var finalResp = _InternalRequest( template: "#uri#/get.cfm", url: getUrlArgs, cookies: cookies );
		var finalData = deserializeJSON( finalResp.fileContent );

		if ( arguments.expectInStorage ) {
			expect( finalData.sessionInStorage ).toBeTrue(
				"BUG LDEV-6331 [#label#]: persisted session entry evicted at original TTL boundary "
				& "despite a mid-TTL read that should have refreshed it."
			);
			expect( finalData.sessionUser ).toBe(
				userValue,
				"BUG LDEV-6331 [#label#]: session payload lost on final GET — expected #userValue#, got #finalData.sessionUser#. "
				& "Storage entry survived but session data did not round-trip."
			);
		} else {
			expect( finalData.sessionInStorage ).toBeFalse(
				"LDEV-6331 [#label#]: opt-out failed — storage entry survived past original TTL even with sessionCommitInterval=0. "
				& "Periodic refresh should be disabled when commitInterval <= 0."
			);
		}
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
