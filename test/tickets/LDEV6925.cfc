component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6925: a locked setting must not block saving the other settings of the same admin page", function() {

			it( title="updateScope should still save mergeUrlForm when sessionType is locked by a system property", body=function() {
				java.lang.System::setProperty( "lucee.sessionType", "cfml" );
				var original = "";
				var flipped = "";
				try {
					original = getScope().mergeFormAndUrl;
					flipped = !original;
					updateScopeWith( { mergeUrlForm: flipped, sessionType: "cfml" } );
					expect( getScope().mergeFormAndUrl ).toBe( flipped );
				}
				finally {
					try {
						if ( !isSimpleValue( original ) || original != "" ) {
							updateScopeWith( { mergeUrlForm: original, sessionType: "cfml" } );
						}
					}
					catch ( any e ) {}
					java.lang.System::clearProperty( "lucee.sessionType" );
				}
			});

			it( title="updateScope should still throw when the locked sessionType value is actually changed", body=function() {
				java.lang.System::setProperty( "lucee.sessionType", "cfml" );
				try {
					var msg = "";
					try {
						updateScopeWith( { sessionType: "j2ee" } );
						fail( "updateScope should have thrown when changing a locked sessionType" );
					}
					catch ( any e ) {
						msg = e.message;
					}
					expect( msg ).toInclude( "defined via the system property" );
					expect( msg ).toInclude( "lucee.sessionType" );
					expect( msg ).toInclude( "sessionType" );
				}
				finally {
					java.lang.System::clearProperty( "lucee.sessionType" );
				}
			});

		});
	}

	private function getScope() {
		admin
			action="getScope"
			type="server"
			password=request.SERVERADMINPASSWORD
			returnVariable="local.scope";
		return scope;
	}

	private function updateScopeWith( required struct overrides ) {
		var scope = getScope();
		var args = {
			action: "updateScope",
			type: "server",
			password: request.SERVERADMINPASSWORD,
			sessionType: scope.sessiontype,
			localScopeMode: scope.localmode,
			scopeCascadingType: scope.scopeCascadingType,
			cascadeToResultset: scope.allowImplicidQueryCall,
			mergeUrlForm: scope.mergeFormAndUrl,
			formUrlAsStruct: scope.formUrlAsStruct,
			clientTimeout: scope.clientTimeout,
			sessionTimeout: scope.sessionTimeout,
			applicationTimeout: scope.applicationTimeout,
			sessionManagement: scope.sessionManagement,
			clientManagement: scope.clientManagement,
			clientCookies: scope.clientCookies,
			domaincookies: scope.domainCookies,
			sessionStorage: scope.sessionStorage,
			clientStorage: scope.clientStorage,
			cgiScopeReadOnly: scope.cgiReadonly
		};
		structAppend( args, arguments.overrides, true );
		admin attributeCollection=args;
	}

}
