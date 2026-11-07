component extends="org.lucee.cfml.test.LuceeTestCase" labels="lar,archive,jakarta" {

	function run( testResults, testBox ) {
		describe( "LDEV-6171: LAR archives must not contain javax.servlet references", function() {

			it( "bundled LAR archives should only reference jakarta, not javax.servlet", function() {
				var larNames = [ "lucee-context.lar", "lucee-admin.lar", "lucee-doc.lar" ];
				var javaxRefs = [];

				for ( var larName in larNames ) {
					var resPath = "/resource/context/" & larName;
					var is = getPageContext().getClass().getResourceAsStream( resPath );
					if ( isNull( is ) ) continue;
					try {
						var zis = createObject( "java", "java.util.zip.ZipInputStream" ).init( is );
						var entry = zis.getNextEntry();
						while ( !isNull( entry ) ) {
							if ( entry.getName().endsWith( ".class" ) ) {
								var bos = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
								var buf = createObject( "java", "java.lang.reflect.Array" ).newInstance( createObject( "java", "java.lang.Byte" ).TYPE, 8192 );
								var len = zis.read( buf );
								while ( len != -1 ) {
									bos.write( buf, 0, len );
									len = zis.read( buf );
								}
								// the references to a class are in the constant pool as plain text
								if ( createObject( "java", "java.lang.String" ).init( bos.toByteArray(), "ISO-8859-1" ).contains( "javax/servlet" ) ) {
									javaxRefs.append( larName & ": " & entry.getName() );
								}
							}
							entry = zis.getNextEntry();
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
