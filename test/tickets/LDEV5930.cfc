component extends="org.lucee.cfml.test.LuceeTestCase" labels="session,mysql" skip=true {

	// LDEV-5930: Component properties stored in session scope are lost when
	// using database-backed session storage and the in-memory session is evicted.
	// Changes to complex objects (components via accessors) are not detected
	// because only direct session key modifications set the hasChanges flag.

	function isMySqlNotSupported() {
		var mySql = server.getDatasource( "mysql" );
		return isEmpty( mysql );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5930: session component property change detection with DB storage", function() {

			it( title="component property changes via accessor should survive memory eviction (sessionCluster=false)",
				skip=isMySqlNotSupported(),
				body=function( currentSpec ) {
				var uri = createURI( "LDEV5930" );

				// Request 1: Create session - onSessionStart stores a component with default values
				var result1 = _InternalRequest(
					template: "#uri#/createSession.cfm"
				);
				var data1 = deserializeJSON( result1.filecontent.trim() );
				expect( data1.username ).toBe( "" );
				expect( data1.role ).toBe( "guest" );

				var cfid = data1.cfid;
				var cftoken = data1.cftoken;

				// Request 2: Modify component properties via accessors (NOT a direct session key change)
				var result2 = _InternalRequest(
					template: "#uri#/modifyProperty.cfm",
					url: { username: "zac", role: "admin" },
					cookies: { cfid: cfid, cftoken: cftoken }
				);
				var data2 = deserializeJSON( result2.filecontent.trim() );
				expect( data2.username ).toBe( "zac" );
				expect( data2.role ).toBe( "admin" );

				// Request 3: Evict session from memory - simulates the ~2 minute idle eviction
				var result3 = _InternalRequest(
					template: "#uri#/evictSession.cfm",
					cookies: { cfid: cfid, cftoken: cftoken }
				);
				var data3 = deserializeJSON( result3.filecontent.trim() );
				expect( data3.evicted ).toBeTrue();

				// Request 4: Read back properties - session must reload from DB
				// This is the critical test: were the property changes persisted to DB?
				var result4 = _InternalRequest(
					template: "#uri#/readProperty.cfm",
					cookies: { cfid: cfid, cftoken: cftoken }
				);
				var data4 = deserializeJSON( result4.filecontent.trim() );
				expect( data4.username ).toBe( "zac", "Component property 'username' should persist after memory eviction" );
				expect( data4.role ).toBe( "admin", "Component property 'role' should persist after memory eviction" );
			});

			it( title="component property changes via accessor should persist with sessionCluster=true",
				skip=true,
				body=function( currentSpec ) {
				var uri = createURI( "LDEV5930" );

				// Request 1: Create session with sessionCluster=true
				var result1 = _InternalRequest(
					template: "#uri#/createSession.cfm",
					url: { sessionCluster: true }
				);
				var data1 = deserializeJSON( result1.filecontent.trim() );
				expect( data1.username ).toBe( "" );
				expect( data1.role ).toBe( "guest" );

				var cfid = data1.cfid;
				var cftoken = data1.cftoken;

				// Request 2: Modify component properties
				var result2 = _InternalRequest(
					template: "#uri#/modifyProperty.cfm",
					url: { username: "test_user", role: "editor", sessionCluster: true },
					cookies: { cfid: cfid, cftoken: cftoken }
				);

				// Request 3: Read back - sessionCluster=true forces DB reload every request
				var result3 = _InternalRequest(
					template: "#uri#/readProperty.cfm",
					url: { sessionCluster: true },
					cookies: { cfid: cfid, cftoken: cftoken }
				);
				var data3 = deserializeJSON( result3.filecontent.trim() );
				expect( data3.username ).toBe( "test_user", "Component property should persist with sessionCluster=true" );
				expect( data3.role ).toBe( "editor", "Component property should persist with sessionCluster=true" );
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}
}
