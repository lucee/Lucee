component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV6882" {

	function beforeAll() {
		variables.adminRoot = getAdminRoot();
		variables.password = server.SERVERADMINPASSWORD;
		variables.adm = new org.lucee.cfml.Administrator( "server", variables.password );
		variables.directJavaAccess = variables.adm.getDefaultSecurityManager().direct_java_access;
	}

	function afterAll() {
		variables.adm.updateDefaultSecurityManager( direct_java_access: variables.directJavaAccess );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6882 admin with direct java access disabled", skip=noAdmin(), body=function() {

			it( title="systemSetting.cfm renders the stored timespan values, with and without direct java access", body=function( currentSpec ) {
				// includes values which are not normalized, these have to stay as they are
				var timeSpans = [
					{ day: 1, hour: 2, minute: 3, second: 4, milli: 0 },
					{ day: 0, hour: 0, minute: 90, second: 0, milli: 0 },
					{ day: 0, hour: 25, minute: 0, second: 0, milli: 0 },
					{ day: 0, hour: 0, minute: 0, second: 90, milli: 0 },
					{ day: 0, hour: 0, minute: 0, second: 50, milli: 0 },
					{ day: 0, hour: 0, minute: 0, second: 5, milli: 500 },
					{ day: 0, hour: 0, minute: 0, second: 0, milli: 0 }
				];

				var withJava = [];
				var expected = [];
				loop array=timeSpans item="local.ts" {
					// reading the properties directly is what systemSetting.cfm did before this ticket
					var real = createTimeSpan( ts.day, ts.hour, ts.minute, ts.second, ts.milli );
					expected.append( { day: real.day, hour: real.hour, minute: real.minute, second: real.second } );
					withJava.append( renderSystemSetting( ts ) );
				}

				var withoutJava = [];
				var javaBlocked = false;
				variables.adm.updateDefaultSecurityManager( direct_java_access: false );
				try {
					javaBlocked = isJavaBlocked();
					loop array=timeSpans item="local.ts" {
						withoutJava.append( renderSystemSetting( ts ) );
					}
				}
				finally {
					variables.adm.updateDefaultSecurityManager( direct_java_access: true );
				}

				expect( javaBlocked ).toBeTrue( "direct java access should be blocked" );
				loop from=1 to=timeSpans.len() index="local.i" {
					expectRendered( withJava[ i ], expected[ i ] );
					expectRendered( withoutJava[ i ], expected[ i ] );
				}
				expect( expected[ 2 ].minute ).toBe( 90 );
				expect( expected[ 2 ].hour ).toBe( 0 );
			});

			it( title="login and the affected admin pages work without direct java access", body=function( currentSpec ) {
				var pages = [
					"index.cfm?action=overview",
					"index.cfm?action=security.password",
					"index.cfm?action=server.scope",
					"index.cfm?action=services.certificates"
				];
				var cookies = login();
				var results = {};
				var loginResult = "";
				var loginDelay = variables.adm.getLoginSettings().loginDelay;

				variables.adm.updateDefaultSecurityManager( direct_java_access: false );
				try {
					loop array=pages item="local.page" {
						results[ page ] = checkPage( page, cookies );
					}
					// a login attempt within the login delay is rejected
					sleep( ( loginDelay + 1 ) * 1000 );
					loginResult = checkLogin();
				}
				finally {
					variables.adm.updateDefaultSecurityManager( direct_java_access: true );
				}

				loop array=pages item="local.page" {
					expect( results[ page ] ).toBe( "", page );
				}
				expect( loginResult ).toBe( "", "login" );
			});

		});
	}

	private string function renderSystemSetting( required struct ts ) {
		var params = duplicate( arguments.ts );
		params.adminRoot = variables.adminRoot;
		var result = _internalRequest(
			template: createURI( "LDEV6882/systemSetting.cfm" ),
			urls: params
		);
		return result.fileContent;
	}

	private void function expectRendered( required string content, required struct expected ) {
		var e = arguments.expected;
		var parts = "#e.day#,#e.hour#,#e.minute#,#e.second#";
		expect( arguments.content ).toInclude( 'name="clientTimeout_days" value="#e.day#"' );
		expect( arguments.content ).toInclude( 'name="clientTimeout_hours" value="#e.hour#"' );
		expect( arguments.content ).toInclude( 'name="clientTimeout_minutes" value="#e.minute#"' );
		expect( arguments.content ).toInclude( 'name="clientTimeout_seconds" value="#e.second#"' );
		expect( arguments.content ).toInclude( "createTimeSpan(#parts#)" );
		expect( arguments.content ).toInclude( "<settings>#parts#</settings>" );
	}

	private string function checkPage( required string page, required struct cookies ) {
		try {
			var result = _internalRequest(
				template: variables.adminRoot & listFirst( arguments.page, "?" ),
				urls: listRest( arguments.page, "?" ) & "&rawError=true",
				cookies: arguments.cookies
			);
			if ( result.status != 200 ) return "status " & result.status;
			if ( findNoCase( "login_password", result.fileContent ) ) return "not logged in";
			return "";
		}
		catch ( any e ) {
			return e.message;
		}
	}

	private string function checkLogin() {
		try {
			login();
			return "";
		}
		catch ( any e ) {
			return e.message;
		}
	}

	private struct function login() {
		var result = _internalRequest(
			template: variables.adminRoot & "index.cfm",
			forms: { login_passwordserver: variables.password, lang: "en", rememberMe: "s", submit: "submit" }
		);
		if ( findNoCase( "login_password", result.fileContent ) ) throw( message="login failed" );
		return { cfid: result.session.cfid, cftoken: result.session.cftoken };
	}

	private boolean function isJavaBlocked() {
		try {
			var meta = getMetaData( "" );
			return isNull( meta.name );
		}
		catch ( any e ) {
			return true;
		}
	}

	private string function getAdminRoot() {
		var adminRoot = server.system.environment.LUCEE_TEST_ADMIN_PATH ?: "";
		if ( len( adminRoot ) ) return adminRoot;
		for ( adminRoot in [ "/admin/", "/lucee/admin/" ] ) {
			if ( !isNull( getPageContext().getRelativePageSourceExisting( adminRoot & "index.cfm" ) ) ) return adminRoot;
		}
		return "";
	}

	private boolean function noAdmin() {
		return !len( getAdminRoot() );
	}

	private string function createURI( required string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}
