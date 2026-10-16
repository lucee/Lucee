component extends="org.lucee.cfml.test.LuceeTestCase" labels="lar,archive,jakarta" {

	function run( testResults, testBox ) {
		describe( "LDEV-6171: LAR archives must not contain javax.servlet references", function() {

			it( "bundled LAR archives should only reference jakarta, not javax.servlet", function() {
				var larNames = [ "lucee-context.lar", "lucee-admin.lar", "lucee-doc.lar" ];
				var JarUtil = createObject( "java", "lucee.runtime.osgi.JarUtil" );
				var javaxRefs = [];

				for ( var larName in larNames ) {
					var resPath = "/resource/context/" & larName;
					var is = getPageContext().getClass().getResourceAsStream( resPath );
					if ( isNull( is ) ) continue;
					try {
						var imports = JarUtil.getExternalImports( is, [] );
						for ( var pkg in imports ) {
							if ( pkg.startsWith( "javax.servlet" ) ) {
								javaxRefs.append( larName & ": " & pkg );
							}
						}
					}
					finally {
						is.close();
					}
				}

				expect( javaxRefs ).toHaveLength( 0,
					"Found javax.servlet references in LAR archives: " & javaxRefs.toList( chr( 10 ) ) );
			});

		});
	}

}
