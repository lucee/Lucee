component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV6967" {

	function beforeAll() {
		variables.admin = new org.lucee.cfml.Administrator( "server", request.ServerAdminPassword );
		variables.origComponent = admin.getComponent();
		variables.origOutput = admin.getOutputSetting();
		variables.origPerformance = admin.getPerformanceSettings();
		variables.origCompiler = admin.getCompilerSettings();
		variables.origProxy = admin.getProxy();
	}

	function afterAll() {
		admin.updateComponent(
			componentLocalSearch = origComponent.componentLocalSearch,
			componentPathCache = origComponent.componentPathCache
		);
		admin.updateOutputSetting( bufferOutput = origOutput.bufferOutput );
		admin.updatePerformanceSettings(
			inspectTemplate = origPerformance.inspectTemplate,
			typeChecking = origPerformance.typeChecking
		);
		admin.updateCompilerSettings( suppressWSBeforeArg = origCompiler.suppressWSBeforeArg );
		if ( len( origProxy.server ) ) {
			admin.updateProxy(
				proxyEnabled = true,
				proxyServer = origProxy.server,
				proxyPort = val( origProxy.port ),
				proxyUsername = origProxy.username,
				proxyPassword = origProxy.password
			);
		}
		else {
			admin.removeProxy();
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6967 Administrator.cfc partial updates keep the other settings", function() {

			it( "updateComponent keeps the settings that are not passed", function() {
				admin.updateComponent( componentPathCache = true );
				admin.updateComponent( componentLocalSearch = !origComponent.componentLocalSearch );

				var component = admin.getComponent();
				expect( component.componentPathCache ).toBeTrue();
				expect( component.componentLocalSearch ).toBe( !origComponent.componentLocalSearch );
				expect( component.useShadow ).toBe( origComponent.useShadow );
				expect( component.componentDataMemberDefaultAccess ).toBe( origComponent.componentDataMemberDefaultAccess );
				expect( component.returnFormat ).toBe( origComponent.returnFormat );
			});

			it( "updateOutputSetting works without cfmlWriter", function() {
				admin.updateOutputSetting( bufferOutput = !origOutput.bufferOutput );

				var output = admin.getOutputSetting();
				expect( output.bufferOutput ).toBe( !origOutput.bufferOutput );
				expect( output.cfmlWriter ).toBe( origOutput.cfmlWriter );
			});

			it( "updatePerformanceSettings works without inspectTemplate", function() {
				admin.updatePerformanceSettings( typeChecking = !origPerformance.typeChecking );

				var performance = admin.getPerformanceSettings();
				expect( performance.typeChecking ).toBe( !origPerformance.typeChecking );
				expect( performance.inspectTemplate ).toBe( origPerformance.inspectTemplate );
				expect( performance.inspectTemplateIntervalSlow ).toBe( origPerformance.inspectTemplateIntervalSlow );
				expect( performance.inspectTemplateIntervalFast ).toBe( origPerformance.inspectTemplateIntervalFast );
			});

			it( "updateCompilerSettings works without templateCharset and dotNotationUpperCase", function() {
				admin.updateCompilerSettings( suppressWSBeforeArg = !origCompiler.suppressWSBeforeArg );

				var compiler = admin.getCompilerSettings();
				expect( compiler.suppressWSBeforeArg ).toBe( !origCompiler.suppressWSBeforeArg );
				expect( toString( compiler.templateCharset ) ).toBe( toString( origCompiler.templateCharset ) );
				expect( compiler.dotNotationUpperCase ).toBe( origCompiler.dotNotationUpperCase );
			});

			it( "updateProxy keeps the port and removeProxy removes the proxy", function() {
				admin.updateProxy( proxyEnabled = true, proxyServer = "proxy.ldev6967.test", proxyPort = 3128 );
				admin.updateProxy( proxyUsername = "ldev6967" );

				var proxy = admin.getProxy();
				expect( proxy.server ).toBe( "proxy.ldev6967.test" );
				expect( proxy.port ).toBe( "3128" );
				expect( proxy.username ).toBe( "ldev6967" );

				admin.removeProxy();
				expect( admin.getProxy().server ).toBe( "" );
			});
		});
	}
}
