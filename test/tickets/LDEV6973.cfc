component extends="org.lucee.cfml.test.LuceeTestCase" labels="image,extension" {

	// a page compiled against the image extension 2.0.0.26 (OSGi bundle "org.lucee.image.extension") calls
	// exactly these methods, the extension installed now (3.1+) only provides the Maven artifact "org.lucee:image"
	variables.OLD_BUNDLE = "org.lucee.image.extension";
	variables.OLD_VERSION = "2.0.0.26";

	function run( testResults, testBox ) {
		describe( "LDEV-6973: compiled code referencing an OSGi bundle that was replaced by a Maven artifact", function() {

			it( "maps the old OSGi bundle names of extensions to their Maven artifact", function() {
				var m = createObject( "java", "lucee.runtime.osgi.OSGiToMaven" );
				expect( m.getMavenFor( "org.lucee.chart.extension" ) ).toBe( "org.lucee:chart" );
				expect( m.getMavenFor( "org.lucee.image.extension" ) ).toBe( "org.lucee:image" );
				expect( m.getMavenFor( "ORG.LUCEE.PDF.EXTENSION" ) ).toBe( "org.lucee:pdf" );
				expect( isNull( m.getMavenFor( "org.lucee.ldev6973.notmapped" ) ) ).toBeTrue();
			});

			it( title="function: loads the class from the installed Maven artifact", skip=noMavenImage(), body=function() {
				var pc = getPageContext();
				var args = javacast( "java.lang.Object[]", [ "", 11, 22 ] );
				var img = createObject( "java", "lucee.runtime.functions.FunctionHandlerPool" )
					.invoke( pc, args, "org.lucee.extension.image.functions.ImageNew", variables.OLD_BUNDLE, variables.OLD_VERSION );
				expect( imageGetWidth( img ) ).toBe( 11 );
				expect( imageGetHeight( img ) ).toBe( 22 );
			});

			it( title="tag: resolves a renamed tag class (tag.Image -> tag.jakarta.Image) and reuses it", skip=noMavenImage(), body=function() {
				var pc = getPageContext();
				var tag = pc.use( "org.lucee.extension.image.tag.Image", variables.OLD_BUNDLE, variables.OLD_VERSION, "cfimage", -1 );
				expect( tag.getClass().getName() ).toBe( "org.lucee.extension.image.tag.jakarta.Image" );
				pc.reuse( tag, variables.OLD_BUNDLE, variables.OLD_VERSION );
				var tag2 = pc.use( "org.lucee.extension.image.tag.Image", variables.OLD_BUNDLE, variables.OLD_VERSION, "cfimage", -1 );
				pc.reuse( tag2, variables.OLD_BUNDLE, variables.OLD_VERSION );
				expect( tag2 === tag ).toBeTrue( "the tag should go back to the pool it was requested from" );
			});

			it( title="maven: a version that is not installed falls back to the installed one", skip=noMavenImage(), body=function() {
				var pc = getPageContext();
				var mvn = "org.lucee:image:0.0.0.LDEV6973";
				var args = javacast( "java.lang.Object[]", [ "", 5, 6 ] );
				var img = createObject( "java", "lucee.runtime.functions.FunctionHandlerPool" )
					.invoke( pc, args, "org.lucee.extension.image.functions.ImageNew", mvn );
				expect( imageGetWidth( img ) ).toBe( 5 );
				var tag = pc.use( "org.lucee.extension.image.tag.jakarta.Image", mvn, "cfimage", -1 );
				pc.reuse( tag, mvn );
				expect( tag.getClass().getName() ).toBe( "org.lucee.extension.image.tag.jakarta.Image" );
			});

		});
	}

	// the image extension needs to be installed as Maven artifact (image 3.1+)
	private boolean function noMavenImage() {
		try {
			var installed = false;
			var cfg = getPageContext().getConfig();
			loop array=cfg.getTLDs() item="local.tl" {
				var tlt = tl.getTag( "image" );
				if ( !isNull( tlt ) && find( "org.lucee:image:", tlt.getTagClassDefinition().toString() ) ) installed = true;
			}
			return !installed;
		}
		catch ( e ) {
			return true;
		}
	}
}
