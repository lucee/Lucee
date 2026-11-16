component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	function beforeAll() {
		variables.adminRoot = getAdminRoot();
		// ConfigServerImpl.metaCustomTagExtensions is backed by the system property "lucee.customTagExtensions"
		variables.propName = "lucee.customTagExtensions";
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6924 - Custom Tags settings update when the extensions row is locked by system property / env var", skip=noAdmin(), body=function() {

			beforeEach( function() {
				variables.original = getCustomTagSetting();
			});

			afterEach( function() {
				java.lang.System::clearProperty( propName );
				admin action="updateCustomTagSetting" type="server" password=server.SERVERADMINPASSWORD
					customTagDeepSearch=original.customTagDeepSearch
					customTagLocalSearch=original.customTagLocalSearch
					customTagUseCachePath=original.customTagUseCachePath
					customTagExtensions=arrayToList( original.customTagExtensions );
			});

			it( title="saves the settings when only the hidden customTagExtensions field is submitted instead of the extensions radios", body=function() {
				var newLocalSearch = !original.customTagLocalSearch;
				var result = postSettings( newLocalSearch, arrayToList( original.customTagExtensions ) );

				expect( result.fileContent ).notToInclude( "[EXTENSIONS]" );
				expect( getCustomTagSetting().customTagLocalSearch ).toBe( newLocalSearch );
			});

			it( title="saves the settings when customTagExtensions is locked by the system property", body=function() {
				java.lang.System::setProperty( propName, "cfm,cfc" );
				var newLocalSearch = !original.customTagLocalSearch;
				var result = postSettings( newLocalSearch, "cfm,cfc" );

				expect( result.fileContent ).notToInclude( "[EXTENSIONS]" );
				expect( result.fileContent ).notToInclude( "cannot update the setting" );
				expect( getCustomTagSetting().customTagLocalSearch ).toBe( newLocalSearch );
			});

		});
	}

	// posts the Custom Tags settings form like the browser does when the extensions row is locked:
	// systemSetting.cfm renders a hidden customTagExtensions field instead of the extensions radios
	private struct function postSettings( required boolean localSearch, required string extensions ) {
		var forms = {
			mainAction: "Update",
			subAction: "setting",
			customTagExtensions: arguments.extensions
		};
		if ( original.customTagDeepSearch ) forms.customTagDeepSearch = "yes";
		if ( arguments.localSearch ) forms.customTagLocalSearch = "yes";
		if ( original.customTagUseCachePath ) forms.customTagUseCachePath = "yes";

		return _internalRequest(
			template: adminRoot & "index.cfm",
			urls: { action: "resources.customtags" },
			forms: forms,
			cookies: login()
		);
	}

	private struct function getCustomTagSetting() {
		admin action="getCustomTagSetting" type="server" password=server.SERVERADMINPASSWORD returnVariable="local.setting";
		return setting;
	}

	private struct function login() {
		var result = _internalRequest(
			template: variables.adminRoot & "index.cfm",
			forms: { login_passwordserver: server.SERVERADMINPASSWORD, lang: "en", rememberMe: "s", submit: "submit" }
		);
		if ( findNoCase( "login_password", result.fileContent ) ) throw( message="login failed" );
		return { cfid: result.session.cfid, cftoken: result.session.cftoken };
	}

	// the CI build maps the admin source to /admin/ (see AdminPages.cfc), a plain jar serves it from /lucee/admin/ (archive),
	// light builds (e.g. script-runner in the extension CIs) have no admin at all, returns "" then
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
}
