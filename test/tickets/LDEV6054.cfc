component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6054 Extension startup-hook with maven coordinates", function() {

			it( "ClassDefinitionImpl.toClassDefinition() should recognise OSGi bundle definitions", function() {
				var ClassDefinitionImpl = createObject( "java", "lucee.transformer.library.ClassDefinitionImpl" );

				var map = {
					"class": "org.example.MyStartupHook",
					"bundleName": "org.example.mybundle",
					"bundleVersion": "1.0.0"
				};

				var cd = ClassDefinitionImpl.toClassDefinition( map, false, nullValue() );

				expect( cd ).notToBeNull();
				expect( cd.getClassName() ).toBe( "org.example.MyStartupHook" );
				expect( cd.isBundle() ).toBeTrue();
				expect( cd.isMaven() ).toBeFalse();
				expect( cd.getName() ).toBe( "org.example.mybundle" );
			});

			it( "ClassDefinitionImpl.toClassDefinition() should recognise Maven coordinate definitions", function() {
				var ClassDefinitionImpl = createObject( "java", "lucee.transformer.library.ClassDefinitionImpl" );

				var map = {
					"class": "org.lucee.extension.debugger.extension.ExtensionActivator",
					"maven": "org.lucee:debugger:3.0.0.0"
				};

				var cd = ClassDefinitionImpl.toClassDefinition( map, false, nullValue() );

				expect( cd ).notToBeNull();
				expect( cd.getClassName() ).toBe( "org.lucee.extension.debugger.extension.ExtensionActivator" );
				expect( cd.isBundle() ).toBeFalse();
				expect( cd.isMaven() ).toBeTrue();
				expect( cd.getMavenRaw() ).toBe( "org.lucee:debugger:3.0.0.0" );
			});

			it( "startup hook validation should accept both OSGi and Maven definitions", function() {
				var ClassDefinitionImpl = createObject( "java", "lucee.transformer.library.ClassDefinitionImpl" );

				// OSGi definition
				var osgiMap = {
					"class": "org.example.OsgiHook",
					"bundleName": "org.example.bundle",
					"bundleVersion": "1.0.0"
				};
				var osgiCd = ClassDefinitionImpl.toClassDefinition( osgiMap, false, nullValue() );

				// Maven definition
				var mavenMap = {
					"class": "org.example.MavenHook",
					"maven": "org.example:hook:1.0.0"
				};
				var mavenCd = ClassDefinitionImpl.toClassDefinition( mavenMap, false, nullValue() );

				// Both should pass the validation check used in ConfigAdmin
				// The check is: cd.isBundle() || cd.isMaven()
				expect( osgiCd.isBundle() || osgiCd.isMaven() ).toBeTrue();
				expect( mavenCd.isBundle() || mavenCd.isMaven() ).toBeTrue();
			});

			it( "startup hook validation should reject definitions without bundle or maven", function() {
				var ClassDefinitionImpl = createObject( "java", "lucee.transformer.library.ClassDefinitionImpl" );

				// Definition with only class name (no bundle or maven)
				var invalidMap = {
					"class": "org.example.InvalidHook"
				};
				var invalidCd = ClassDefinitionImpl.toClassDefinition( invalidMap, false, nullValue() );

				// Should fail the validation check
				expect( invalidCd.isBundle() || invalidCd.isMaven() ).toBeFalse();
			});

		});
	}

}
