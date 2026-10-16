component extends="org.lucee.cfml.test.LuceeTestCase" labels="session,mysql" {

	// LDEV-6046: sessionInvalidate() not de-activating old session when using DB for session storage
	// Reported: After v6.2.0.321, sessionInvalidate() stopped properly invalidating sessions stored in database
	//
	// The bug: sessionInvalidate() switches the user's cfid token but the old session remains active.
	// If a different browser uses the old cfid cookie value, the user is still logged in.
	// Works fine with sessionStorage = "memory", broken with database storage.

	function isMySqlNotSupported() {
		var mySql = mySqlCredentials();
		return isEmpty( mysql );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6046: sessionInvalidate() with database session storage", function() {

			it( title="sessionInvalidate() should de-activate old session so it cannot be reused", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var uri = createURI( "LDEV6046" );

				// First request - create session with user_id (simulating logged in user)
				var result1 = _InternalRequest(
					template: "#uri#/mysql/createSession.cfm"
				);
				var data1 = deserializeJSON( result1.filecontent.trim() );
				expect( data1.sessionId ).notToBeEmpty();
				expect( data1.user_id ).notToBeEmpty();

				var originalSessionId = data1.sessionId;
				var originalCfid = data1.cfid;
				var originalCftoken = data1.cftoken;

				// Second request - invalidate session (same "browser" - pass cookies)
				var result2 = _InternalRequest(
					template: "#uri#/mysql/invalidateSession.cfm",
					cookies: {
						cfid: originalCfid,
						cftoken: originalCftoken
					}
				);
				var data2 = deserializeJSON( result2.filecontent.trim() );

				// Verify session was invalidated and a new one created
				expect( data2.oldSessionId ).toBe( originalSessionId );
				expect( data2.newSessionId ).notToBe( originalSessionId, "Session ID should change after sessionInvalidate()" );
				expect( data2.hasUserId ).toBeFalse( "New session should not have user_id from old session" );

				// THE CRITICAL TEST: Simulate another browser trying to use the OLD cfid/cftoken
				// This should NOT return the old session with user_id
				var result3 = _InternalRequest(
					template: "#uri#/mysql/accessWithOldCfid.cfm",
					cookies: {
						cfid: originalCfid,
						cftoken: originalCftoken
					}
				);
				var data3 = deserializeJSON( result3.filecontent.trim() );

				// The old session should NOT be accessible - user should not be "logged in"
				expect( data3.hasUserId ).toBeFalse( "Old session should not be accessible after sessionInvalidate() - user_id should not exist" );
			});

			it( title="old session row should be deleted from database after sessionInvalidate()", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var uri = createURI( "LDEV6046" );

				// Create session
				var result1 = _InternalRequest(
					template: "#uri#/mysql/createSession.cfm"
				);
				var data1 = deserializeJSON( result1.filecontent.trim() );
				var originalSessionId = data1.sessionId;
				var originalCfid = data1.cfid;
				var originalCftoken = data1.cftoken;

				// Invalidate session (same browser - pass cookies)
				var result2 = _InternalRequest(
					template: "#uri#/mysql/invalidateSession.cfm",
					cookies: {
						cfid: originalCfid,
						cftoken: originalCftoken
					}
				);

				// Verify old session row is removed from database
				var result3 = _InternalRequest(
					template: "#uri#/mysql/checkSession.cfm",
					url: { sessionId: originalSessionId }
				);
				var data3 = deserializeJSON( result3.filecontent.trim() );
				expect( data3.sessionExists ).toBeFalse( "Old session row should be deleted from cf_session_data table" );
			});

		});

		describe( "LDEV-6046: sessionInvalidate() with database session storage (sessionCluster=false)", function() {

			it( title="sessionInvalidate() should de-activate old session with sessionCluster=false", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var uri = createURI( "LDEV6046" );

				// First request - create session with user_id (simulating logged in user)
				var result1 = _InternalRequest(
					template: "#uri#/mysql/createSession.cfm",
					url: { sessionCluster: false }
				);
				var data1 = deserializeJSON( result1.filecontent.trim() );
				expect( data1.sessionId ).notToBeEmpty();
				expect( data1.user_id ).notToBeEmpty();

				var originalSessionId = data1.sessionId;
				var originalCfid = data1.cfid;
				var originalCftoken = data1.cftoken;

				// Second request - invalidate session (same "browser" - pass cookies)
				var result2 = _InternalRequest(
					template: "#uri#/mysql/invalidateSession.cfm",
					url: { sessionCluster: false },
					cookies: {
						cfid: originalCfid,
						cftoken: originalCftoken
					}
				);
				var data2 = deserializeJSON( result2.filecontent.trim() );

				// Verify session was invalidated and a new one created
				expect( data2.oldSessionId ).toBe( originalSessionId );
				expect( data2.newSessionId ).notToBe( originalSessionId, "Session ID should change after sessionInvalidate()" );
				expect( data2.hasUserId ).toBeFalse( "New session should not have user_id from old session" );

				// THE CRITICAL TEST: Simulate another browser trying to use the OLD cfid/cftoken
				var result3 = _InternalRequest(
					template: "#uri#/mysql/accessWithOldCfid.cfm",
					url: { sessionCluster: false },
					cookies: {
						cfid: originalCfid,
						cftoken: originalCftoken
					}
				);
				var data3 = deserializeJSON( result3.filecontent.trim() );

				// The old session should NOT be accessible - user should not be "logged in"
				expect( data3.hasUserId ).toBeFalse( "Old session should not be accessible after sessionInvalidate() with sessionCluster=false" );
			});

			it( title="old session row should be deleted from database after sessionInvalidate() with sessionCluster=false", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				var uri = createURI( "LDEV6046" );

				// Create session
				var result1 = _InternalRequest(
					template: "#uri#/mysql/createSession.cfm",
					url: { sessionCluster: false }
				);
				var data1 = deserializeJSON( result1.filecontent.trim() );
				var originalSessionId = data1.sessionId;
				var originalCfid = data1.cfid;
				var originalCftoken = data1.cftoken;

				// Invalidate session (same browser - pass cookies)
				var result2 = _InternalRequest(
					template: "#uri#/mysql/invalidateSession.cfm",
					url: { sessionCluster: false },
					cookies: {
						cfid: originalCfid,
						cftoken: originalCftoken
					}
				);

				// Verify old session row is removed from database
				var result3 = _InternalRequest(
					template: "#uri#/mysql/checkSession.cfm",
					url: { sessionId: originalSessionId, sessionCluster: false }
				);
				var data3 = deserializeJSON( result3.filecontent.trim() );
				expect( data3.sessionExists ).toBeFalse( "Old session row should be deleted from cf_session_data table with sessionCluster=false" );
			});

		});

		describe( "LDEV-6046: sessionInvalidate() with MEMORY session storage (control test)", function() {

			it( title="sessionInvalidate() should de-activate old session with memory storage", body=function( currentSpec ) {
				var uri = createURI( "LDEV6046" );

				// First request - create session with user_id (simulating logged in user)
				var result1 = _InternalRequest(
					template: "#uri#/memory/createSession.cfm"
				);
				var data1 = deserializeJSON( result1.filecontent.trim() );
				expect( data1.sessionId ).notToBeEmpty();
				expect( data1.user_id ).notToBeEmpty();

				var originalSessionId = data1.sessionId;
				var originalCfid = data1.cfid;
				var originalCftoken = data1.cftoken;

				// Second request - invalidate session (same "browser" - pass cookies)
				var result2 = _InternalRequest(
					template: "#uri#/memory/invalidateSession.cfm",
					cookies: {
						cfid: originalCfid,
						cftoken: originalCftoken
					}
				);
				var data2 = deserializeJSON( result2.filecontent.trim() );

				// Verify session was invalidated and a new one created
				expect( data2.oldSessionId ).toBe( originalSessionId );
				expect( data2.newSessionId ).notToBe( originalSessionId, "Session ID should change after sessionInvalidate()" );
				expect( data2.hasUserId ).toBeFalse( "New session should not have user_id from old session" );

				// THE CRITICAL TEST: Simulate another browser trying to use the OLD cfid/cftoken
				// With memory storage, this should work correctly
				var result3 = _InternalRequest(
					template: "#uri#/memory/accessWithOldCfid.cfm",
					cookies: {
						cfid: originalCfid,
						cftoken: originalCftoken
					}
				);
				var data3 = deserializeJSON( result3.filecontent.trim() );

				// The old session should NOT be accessible - user should not be "logged in"
				expect( data3.hasUserId ).toBeFalse( "Old session should not be accessible after sessionInvalidate() - user_id should not exist (memory storage)" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

	private struct function mySqlCredentials() {
		return server.getDatasource( "mysql" );
	}

}
