component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	function run( testResults, testBox ) {
		describe( "LDEV-6948 the lib folder of the server config is known to OSGi after the startup", function() {

			// the startup creates the server config with essentialOnly=true and keeps that instance,
			// the lib folder (where the OSGi extension installs its jar) has to be set for it as well
			it( "OSGi knows the lib folder of the server context", function() {
				var clazz = createObject( "java", "java.lang.Class" ).forName( "lucee.runtime.osgi.OSGi" );
				var field = clazz.getDeclaredField( "libDir" );
				field.setAccessible( true );
				var libDir = field.get( javacast( "null", "" ) );
				expect( isNull( libDir ) ).toBeFalse( "OSGi.setLibraryDirectory was not called during the startup" );

				var expected = getPageContext().getConfig().getConfigServerDir().getRealResource( "lib" );
				expect( libDir.getCanonicalPath() ).toBe( createObject( "java", "java.io.File" ).init( expected.getAbsolutePath() ).getCanonicalPath() );
			});

		});
	}

}
