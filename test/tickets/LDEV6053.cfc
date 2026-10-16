component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension" {

	function run( testResults, testBox ) {
		describe( "LDEV-6053 extension images are loaded on demand", function() {

			it( "toStruct() and extensionList() still return the logo of each installed extension", function() {
				var checked = 0;
				var exts = extensionList();
				loop array=getPageContext().getConfig().getRHExtensions() item="local.ext" {
					var expected = readLogo( ext.getExtensionFile() );
					if ( !len( expected ) ) continue;
					checked++;
					expect( ext.toStruct().image ).toBe( expected, ext.getId() );
					var row = queryExecute( "select image from exts where id = :id", { id: ext.getId() }, { dbtype: "query" } );
					expect( row.recordCount ).toBeGTE( 1 );
					expect( row.image ).toBe( expected, ext.getId() );
				}
				if ( checked == 0 ) skip( "no installed extension with META-INF/logo.png" );
			});

		});
	}

	private string function readLogo( file ) {
		if ( isNull( file ) || !file.isFile() ) return "";
		var zip = createObject( "java", "java.util.zip.ZipFile" ).init( file.getAbsolutePath() );
		try {
			var entries = zip.entries();
			while ( entries.hasMoreElements() ) {
				var entry = entries.nextElement();
				if ( !entry.isDirectory() && entry.getName() == "META-INF/logo.png" ) {
					var bytes = createObject( "java", "lucee.commons.io.IOUtil" ).toBytes( zip.getInputStream( entry ), true );
					return arrayLen( bytes ) ? toBase64( bytes ) : "";
				}
			}
		}
		finally {
			zip.close();
		}
		return "";
	}

}
