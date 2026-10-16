component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql,session" {

	/**
	 * LDEV-5964 - IKHandlerDatasource.store() nested connection borrows can deadlock
	 *
	 * When persisting session/client scope data to a datasource, the store() method
	 * acquires a connection and then calls loadData(), which attempts to borrow a
	 * second connection from the same pool. This nested borrowing creates a deadlock
	 * when the pool size is limited to one connection.
	 *
	 * Bug location: IKHandlerDatasource.java
	 * - store() borrows a connection at line ~131
	 * - store() calls loadData() at line ~133
	 * - loadData() tries to borrow another connection at line ~45
	 *
	 * Fix: Pass the already-borrowed connection to loadData() instead of having it borrow its own.
	 */

	function beforeAll() {
		if ( isMySqlNotSupported() ) return;
		variables.creds = mySqlCredentials( true );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-5964 - Session storage nested connection deadlock", function() {

			it( title="session storage with maxTotal=1 should not deadlock", skip=isMySqlNotSupported(), body=function( currentSpec ) {
				// Configure a datasource with maxTotal=1 - only one connection allowed
				var dsConfig = {
					class: "com.mysql.cj.jdbc.Driver",
					bundleName: "com.mysql.cj",
					connectionString: "jdbc:mysql://#creds.server#:#creds.port#/#creds.database#?useSSL=false&allowPublicKeyRetrieval=true",
					username: creds.username,
					password: creds.password,
					storage: true,
					maxTotal: 1,
					maxWaitMillis: 5000 // 5 second timeout to detect deadlock quickly
				};

				application action="update" datasources={ "LDEV5964_ds": dsConfig };

				// Create test via internal request to trigger session storage
				var uri = createURI( "LDEV5964" );
				var startTime = getTickCount();

				var result = _InternalRequest(
					template: "#uri#/test.cfm",
					form: { action: "createSession" }
				);

				var elapsed = getTickCount() - startTime;

				systemOutput( "LDEV-5964 test completed in #elapsed#ms", true );
				systemOutput( "Response: #result.filecontent#", true );

				// If we get here without timeout, the test passed
				// The request should complete quickly (< 3 seconds)
				// With the bug, it would deadlock and hit the 3 second request timeout
				expect( elapsed ).toBeLT( 3000,
					"Request took #elapsed#ms - likely deadlocked waiting for connection. " &
					"store() holds connection while loadData() waits for another from same pool." );

				expect( result.filecontent ).toInclude( "success",
					"Session storage should work without deadlock" );
			});

		});

	}

	function isMySqlNotSupported() {
		return isEmpty( mySqlCredentials() );
	}

	private struct function mySqlCredentials( onlyConfig=false ) {
		return server.getDatasource( service="mysql", onlyConfig=arguments.onlyConfig );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
