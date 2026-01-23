<cfscript>
	// Mimic QrCodeGenerator pattern: DirectoryList to get paths
	libDir = expandPath( "{lucee-server}/context/lib" );
	// Get actual JAR files, not directories
	libs = directoryList( path=libDir, recurse=true, listinfo="path", filter="*.jar" );

	// Use createObject with loadPaths like QR code generator does
	obj = createObject( "java", "java.util.HashMap", libs );

	writeOutput( "OK" );
</cfscript>
