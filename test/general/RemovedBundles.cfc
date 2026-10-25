component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	function beforeAll() {
		// OSGiUtil is a class of the core bundle
		variables.core = createObject( "java", "lucee.runtime.osgi.OSGiUtil" ).getClass().getClassLoader().getBundle();
	}

	private boolean function canLoad( required string className ) {
		try {
			createObject( "java", arguments.className );
			return true;
		}
		catch ( any e ) {
			// only a class that does not exist is a "no", anything else is an error in the test
			if ( isDefined( "e.cause.type" ) && e.cause.type == "java.lang.ClassNotFoundException" ) return false;
			rethrow;
		}
	}

	function run( testResults, testBox ) {
		describe( "commons-email-all and commons-logging are not part of the core anymore", function() {

			it( title="the core does not require the bundles", body=function( currentSpec ) {
				var requires = variables.core.getHeaders().get( "Require-Bundle" ) ?: "";
				expect( requires ).notToInclude( "commons-email" );
				expect( requires ).notToInclude( "commons-logging" );
				var imports = variables.core.getHeaders().get( "Import-Package" ) ?: "";
				expect( imports ).notToInclude( "org.apache.commons.mail" );
				expect( imports ).notToInclude( "org.apache.commons.logging" );
			});

			it( title="the classes are not provided by Lucee", body=function( currentSpec ) {
				// javax.mail / javax.activation are not tested here: an older extension that imports them gets the bundles from Maven (OSGiUtil.packageBundleMapping) and they are visible from then on
				expect( canLoad( "org.apache.commons.mail.Email" ) ).toBeFalse();
			});

			it( title="Lucee does not configure commons-logging for the libraries of extensions", body=function( currentSpec ) {
				var System = createObject( "java", "java.lang.System" );
				for ( var name in [ "org.apache.commons.logging.Log", "org.apache.commons.logging.LogFactory", "org.apache.commons.logging.simplelog.defaultlog", "org.apache.commons.logging.LogFactory.HashtableImpl" ] ) {
					expect( isNull( System.getProperty( name ) ) ).toBeTrue( name );
				}
			});

			it( title="a library that still needs them can bring its own copy from Maven", body=function( currentSpec ) {
				var mail = new component javaSettings='{"maven":["org.apache.commons:commons-email:1.5"]}' {
					function emailClassName() {
						return createObject( "java", "org.apache.commons.mail.SimpleEmail" ).getClass().getName();
					}
				};
				expect( mail.emailClassName() ).toBe( "org.apache.commons.mail.SimpleEmail" );
			});
		});
	}
}
