component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	function beforeAll() {
		variables.orgQueue = getQueue();
	}

	function afterAll() {
		updateQueue( argumentCollection=variables.orgQueue );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6992 disabling the request queue keeps the configured max and timeout", function() {

			it( "disable the queue with max and timeout keeps both values", function() {
				updateQueue( enable=true, max=50, timeout=30000 );
				var qs = getQueue();
				expect( qs.enable ).toBeTrue();
				expect( qs.max ).toBe( 50 );
				expect( qs.timeout ).toBe( 30000 );

				// the admin (server.request.cfm) now sends the current values when the fields are not editable
				updateQueue( enable=false, max=qs.max, timeout=qs.timeout );
				qs = getQueue();
				expect( qs.enable ).toBeFalse();
				expect( qs.max ).toBe( 50 );
				expect( qs.timeout ).toBe( 30000 );
			});

			it( "re-enable the queue uses the values configured before it was disabled", function() {
				updateQueue( enable=true, max=50, timeout=30000 );
				var qs = getQueue();
				updateQueue( enable=false, max=qs.max, timeout=qs.timeout );
				qs = getQueue();
				updateQueue( enable=true, max=qs.max, timeout=qs.timeout );
				qs = getQueue();
				expect( qs.enable ).toBeTrue();
				expect( qs.max ).toBe( 50 );
				expect( qs.timeout ).toBe( 30000 );
			});

		});
	}

	private struct function getQueue() {
		admin
			action="getQueueSetting"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			returnVariable="local.qs";
		return qs;
	}

	private void function updateQueue( required enable, required max, required timeout ) {
		admin
			action="updateQueueSetting"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			enable="#arguments.enable#"
			max="#arguments.max#"
			timeout="#arguments.timeout#";
	}
}
