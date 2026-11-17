component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	function beforeAll() {
		variables.adminRoot = getAdminRoot();
		// log in once, a second admin login within the login delay (default 1s) is rejected
		if ( len( variables.adminRoot ) ) variables.cookies = login();
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6949 - Component, Scope and Request settings save when a setting of the page is locked by system property / env var", skip=noAdmin(), body=function() {

			it( title="Scope: saves the settings when cgiScopeReadOnly is locked", body=function() {
				var original = getScope();
				var newMerge = !original.mergeFormAndUrl;
				java.lang.System::setProperty( "lucee.cgiScopeReadOnly", "true" );
				try {
					// systemSetting.cfm renders a hidden cgiScopeReadOnly field instead of the cgiReadonly radios
					var forms = {
						mainAction: "Update",
						sessionType: original.sessiontype,
						localScopeMode: original.localmode,
						scopeCascading: original.scopeCascadingType,
						sessionStorage: original.sessionStorage,
						clientStorage: original.clientStorage,
						cgiScopeReadOnly: "true"
					};
					for ( var ts in [ "clientTimeout", "sessionTimeout", "applicationTimeout" ] ) {
						forms[ ts & "_days" ] = original[ ts & "_day" ];
						forms[ ts & "_hours" ] = original[ ts & "_hour" ];
						forms[ ts & "_minutes" ] = original[ ts & "_minute" ];
						forms[ ts & "_seconds" ] = original[ ts & "_second" ];
					}
					if ( original.allowImplicidQueryCall ) forms.cascadeToResultset = "true";
					if ( newMerge ) forms.mergeUrlForm = "true";
					if ( original.formUrlAsStruct ) forms.formUrlAsStruct = "true";
					if ( original.sessionManagement ) forms.sessionManagement = "true";
					if ( original.clientManagement ) forms.clientManagement = "true";
					if ( original.clientCookies ) forms.clientCookies = "true";
					if ( original.domainCookies ) forms.domaincookies = "true";

					var result = post( "server.scope", forms );

					expect( result.fileContent ).notToInclude( "cannot update the setting" );
					expect( getScope().mergeFormAndUrl ).toBe( newMerge );
				}
				finally {
					java.lang.System::clearProperty( "lucee.cgiScopeReadOnly" );
					admin action="updateScope" type="server" password=server.SERVERADMINPASSWORD
						sessionType=original.sessiontype
						localScopeMode=original.localmode
						scopeCascadingType=original.scopeCascadingType
						cascadeToResultset=original.allowImplicidQueryCall
						mergeUrlForm=original.mergeFormAndUrl
						formUrlAsStruct=original.formUrlAsStruct
						clientTimeout=original.clientTimeout
						sessionTimeout=original.sessionTimeout
						applicationTimeout=original.applicationTimeout
						sessionManagement=original.sessionManagement
						clientManagement=original.clientManagement
						clientCookies=original.clientCookies
						domaincookies=original.domainCookies
						sessionStorage=original.sessionStorage
						clientStorage=original.clientStorage
						cgiScopeReadOnly=original.cgiReadonly;
				}
			});

			it( title="Component: saves the settings when componentImplicitNotation is locked", body=function() {
				var original = getComponentSetting();
				var newLocalSearch = !original.componentLocalSearch;
				java.lang.System::setProperty( "lucee.componentImplicitNotation", "true" );
				try {
					// systemSetting.cfm renders a hidden componentImplicitNotation field instead of the triggerDataMember checkbox
					var forms = {
						mainAction: "Update",
						componentDumpTemplate: original.strComponentDumpTemplate,
						componentDataMemberAccess: original.componentDataMemberDefaultAccess,
						componentAutoImport: original.importString,
						returnformat: original.returnFormat,
						componentImplicitNotation: "true"
					};
					if ( original.useShadow ) forms.componentUseVariablesScope = "true";
					if ( newLocalSearch ) forms.componentLocalSearch = "true";
					if ( original.componentPathCache ) forms.componentUseCachePath = "true";
					if ( original.deepSearch ) forms.componentDeepSearch = "true";

					var result = post( "resources.component", forms );

					expect( result.fileContent ).notToInclude( "cannot update the setting" );
					expect( getComponentSetting().componentLocalSearch ).toBe( newLocalSearch );
				}
				finally {
					java.lang.System::clearProperty( "lucee.componentImplicitNotation" );
					admin action="updateComponent" type="server" password=server.SERVERADMINPASSWORD
						componentDumpTemplate=original.strComponentDumpTemplate
						componentDataMemberDefaultAccess=original.componentDataMemberDefaultAccess
						triggerDataMember=original.triggerDataMember
						useShadow=original.useShadow
						componentDefaultImport=original.importString
						componentLocalSearch=original.componentLocalSearch
						componentPathCache=original.componentPathCache
						componentDeepSearch=original.deepSearch
						returnformat=original.returnFormat;
				}
			});

			it( title="Request: saves the settings when requestQueueMax is locked", body=function() {
				var originalApp = getApplicationSetting();
				var originalQueue = getQueueSetting();
				var newAllowURL = !originalApp.AllowURLRequestTimeout;
				var lockedMax = originalQueue.requestQueueMax == 150 ? 151 : 150;
				java.lang.System::setProperty( "lucee.queue.max", lockedMax );
				try {
					// systemSetting.cfm renders a hidden requestQueueMax field instead of the ConcurrentRequestMax input
					var forms = {
						mainAction1: "Update",
						scriptProtect: originalApp.scriptProtect,
						requestTimeout_span_days: originalApp.requestTimeout_day,
						requestTimeout_span_hours: originalApp.requestTimeout_hour,
						requestTimeout_span_minutes: originalApp.requestTimeout_minute,
						requestTimeout_span_seconds: originalApp.requestTimeout_second,
						requestTimeout_concurrentrequestthreshold: originalApp.requestTimeoutConcurrentRequestThreshold,
						requestTimeout_cputhreshold: originalApp.requestTimeoutCPUThreshold,
						requestTimeout_memorythreshold: originalApp.requestTimeoutMemoryThreshold,
						requestQueueTimeout_days: 0,
						requestQueueTimeout_hours: 0,
						requestQueueTimeout_minutes: 0,
						requestQueueTimeout_seconds: int( originalQueue.requestQueueTimeout / 1000 ),
						requestQueueMax: lockedMax
					};
					if ( newAllowURL ) forms.requestTimeoutInURL = "true";
					if ( originalQueue.requestQueueEnable ) forms.requestQueueEnable = "true";

					var result = post( "server.request", forms );

					expect( result.fileContent ).notToInclude( "cannot update the setting" );
					expect( getApplicationSetting().AllowURLRequestTimeout ).toBe( newAllowURL );
				}
				finally {
					java.lang.System::clearProperty( "lucee.queue.max" );
					admin action="updateApplicationSetting" type="server" password=server.SERVERADMINPASSWORD
						scriptProtect=originalApp.scriptProtect
						AllowURLRequestTimeout=originalApp.AllowURLRequestTimeout
						requestTimeout=originalApp.requestTimeout
						requestTimeoutConcurrentRequestThreshold=originalApp.requestTimeoutConcurrentRequestThreshold
						requestTimeoutCPUThreshold=originalApp.requestTimeoutCPUThreshold
						requestTimeoutMemoryThreshold=originalApp.requestTimeoutMemoryThreshold;
					admin action="updateQueueSetting" type="server" password=server.SERVERADMINPASSWORD
						requestQueueMax=originalQueue.requestQueueMax
						requestQueueTimeout=originalQueue.requestQueueTimeout
						requestQueueEnable=originalQueue.requestQueueEnable;
				}
			});

		});
	}

	private struct function post( required string action, required struct forms ) {
		return _internalRequest(
			template: variables.adminRoot & "index.cfm",
			urls: { action: arguments.action },
			forms: arguments.forms,
			cookies: variables.cookies
		);
	}

	private struct function getScope() {
		admin action="getScope" type="server" password=server.SERVERADMINPASSWORD returnVariable="local.scope";
		return scope;
	}

	private struct function getComponentSetting() {
		admin action="getComponent" type="server" password=server.SERVERADMINPASSWORD returnVariable="local.setting";
		var imp = setting.ComponentDefaultImport;
		setting.importString = isSimpleValue( imp ) ? imp : imp.getPackage() & "." & imp.getName();
		return setting;
	}

	private struct function getApplicationSetting() {
		admin action="getApplicationSetting" type="server" password=server.SERVERADMINPASSWORD returnVariable="local.setting";
		return setting;
	}

	private struct function getQueueSetting() {
		admin action="getQueueSetting" type="server" password=server.SERVERADMINPASSWORD returnVariable="local.setting";
		return setting;
	}

	private struct function login() {
		var result = _internalRequest(
			template: variables.adminRoot & "index.cfm",
			forms: { login_passwordserver: server.SERVERADMINPASSWORD, lang: "en", rememberMe: "s", submit: "submit" }
		);
		// another test may have logged in to the admin just before, wait for the login delay and try once more
		if ( findNoCase( "login_password", result.fileContent ) ) {
			admin action="getLoginSettings" type="server" password=server.SERVERADMINPASSWORD returnVariable="local.loginSettings";
			sleep( ( loginSettings.loginDelay + 1 ) * 1000 );
			result = _internalRequest(
				template: variables.adminRoot & "index.cfm",
				forms: { login_passwordserver: server.SERVERADMINPASSWORD, lang: "en", rememberMe: "s", submit: "submit" }
			);
		}
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
