component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	// note: the request queue gets enabled (max 50, timeout 30s) on the test server while this spec runs,
	// with that timeout parallel requests could only wait for a short moment, afterAll restores the original setting

	function beforeAll() {
		variables.orgQueue = getQueue();

		variables.adminRoot = "/admin/";
		if ( !isEmpty( server.system.environment.LUCEE_TEST_ADMIN_PATH ?: "" ) ) {
			variables.adminRoot = server.system.environment.LUCEE_TEST_ADMIN_PATH;
		}
		variables.adminPage = "index.cfm";
		variables.cookies = {};
	}

	function afterAll() {
		// the specs are skipped and the effective values come from the env var / system property, nothing to restore
		if ( hasQueueEnvVar() ) return;
		// remove the keys from the config when they match the defaults, so the config stays clean
		updateQueue(
			enable = orgQueue.enable ? true : "",
			max = orgQueue.max == 100 ? "" : orgQueue.max,
			timeout = orgQueue.timeout == 0 ? "" : orgQueue.timeout
		);
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6992 admin request settings, disabling the queue keeps max and timeout", skip=hasQueueEnvVar(), body=function() {

			it( "login to the admin", function() {
				var result = _internalRequest(
					template: adminRoot & adminPage,
					forms: {
						login_passwordserver: request.SERVERADMINPASSWORD,
						lang: "en",
						rememberMe: "s",
						submit: "submit"
					}
				);
				expect( result.status ).toBe( 200, "Status code" );
				variables.cookies = {
					cfid: result.session.cfid,
					cftoken: result.session.cftoken
				};
			});

			it( "the admin page sets max and timeout read-only and not disabled", function() {
				var result = _internalRequest(
					template: adminRoot & adminPage,
					urls: "action=server.request",
					cookies: variables.cookies
				);
				expect( result.status ).toBe( 200, "Status code" );
				expect( result.fileContent ).toInclude( "prop('readonly'" );
				expect( result.fileContent ).notToInclude( "##ConcurrentRequestMax').prop('disabled'" );
			});

			it( "disable the queue, max and timeout are not submitted (disabled fields)", function() {
				updateQueue( enable=true, max=50, timeout=30000 );

				// what a browser sends when the max and timeout fields are disabled
				var forms = getRequestSettingForms();
				var result = postRequestSettings( forms );
				expect( result.status ).toBe( 302, "Status code (redirect after a successful update) " & result.fileContent );

				var qs = getQueue();
				expect( qs.enable ).toBeFalse();
				expect( qs.max ).toBe( 50 );
				expect( qs.timeout ).toBe( 30000 );
			});

			it( "disable the queue, max and timeout are submitted (read-only fields)", function() {
				updateQueue( enable=true, max=50, timeout=30000 );

				// what a browser sends when the max and timeout fields are read-only
				var forms = getRequestSettingForms();
				forms[ "ConcurrentRequestMax" ] = 50;
				forms[ "timeout_days" ] = 0;
				forms[ "timeout_hours" ] = 0;
				forms[ "timeout_minutes" ] = 0;
				forms[ "timeout_seconds" ] = 30;
				var result = postRequestSettings( forms );
				expect( result.status ).toBe( 302, "Status code (redirect after a successful update) " & result.fileContent );

				var qs = getQueue();
				expect( qs.enable ).toBeFalse();
				expect( qs.max ).toBe( 50 );
				expect( qs.timeout ).toBe( 30000 );
			});

		});
	}

	// the form of the admin page "Settings / Request" with the current values and without the queue fields
	private struct function getRequestSettingForms() {
		admin
			action="getApplicationSetting"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			returnVariable="local.appSettings";

		var forms = {
			"mainAction1": "update",
			"scriptProtect": appSettings.scriptProtect,
			"request_days": appSettings.requestTimeout_day,
			"request_hours": appSettings.requestTimeout_hour,
			"request_minutes": appSettings.requestTimeout_minute,
			"request_seconds": appSettings.requestTimeout_second
		};
		if ( appSettings.AllowURLRequestTimeout ) forms[ "AllowURLRequestTimeout" ] = true;
		return forms;
	}

	private struct function postRequestSettings( required struct forms ) {
		return _internalRequest(
			template: adminRoot & adminPage,
			method: "POST", // the page only redirects (302) after a POST
			urls: "action=server.request",
			forms: arguments.forms,
			cookies: variables.cookies
		);
	}

	private boolean function hasQueueEnvVar() {
		loop list="enable,max,timeout" item="local.k" {
			if ( len( server.system.environment[ "LUCEE_QUEUE_" & ucase( k ) ] ?: "" ) ) return true;
			if ( len( server.system.environment[ "lucee.queue." & k ] ?: "" ) ) return true;
			if ( len( server.system.properties[ "lucee.queue." & k ] ?: "" ) ) return true;
		}
		return false;
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
