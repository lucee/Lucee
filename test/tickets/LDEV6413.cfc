component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6413 Prefer Maven over OSGi in ClassDefinition resolution", function() {

			it( "ClassDefinitionImpl.toClassDefinition() should prefer Maven when both Maven and OSGi are defined", function() {
				var ClassDefinitionImpl = createObject( "java", "lucee.transformer.library.ClassDefinitionImpl" );

				var map = {
					"class": "org.example.MyHandler",
					"bundleName": "org.example.bundle",
					"bundleVersion": "1.0.0",
					"maven": "org.example:handler:1.0.0"
				};

				var cd = ClassDefinitionImpl.toClassDefinition( map, false, nullValue() );

				expect( cd ).notToBeNull();
				expect( cd.getClassName() ).toBe( "org.example.MyHandler" );
				expect( cd.isMaven() ).toBeTrue();
				expect( cd.isBundle() ).toBeFalse();
				expect( cd.getMavenRaw() ).toBe( "org.example:handler:1.0.0" );
			});

			it( "ClassDefinitionImpl.toClassDefinition() should still use OSGi when only bundle info is defined", function() {
				var ClassDefinitionImpl = createObject( "java", "lucee.transformer.library.ClassDefinitionImpl" );

				var map = {
					"class": "org.example.MyHandler",
					"bundleName": "org.example.bundle",
					"bundleVersion": "1.0.0"
				};

				var cd = ClassDefinitionImpl.toClassDefinition( map, false, nullValue() );

				expect( cd ).notToBeNull();
				expect( cd.isBundle() ).toBeTrue();
				expect( cd.isMaven() ).toBeFalse();
				expect( cd.getName() ).toBe( "org.example.bundle" );
			});

		});
	}

}
