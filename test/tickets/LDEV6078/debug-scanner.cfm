<cfscript>
// Debug script to see what ASM output actually looks like
adminPwd = "admin";

// Compile a test file first
admin action="updateMapping" type="server" password="#adminPwd#"
	virtual="/LDEV6078-debug"
	physical=getDirectoryFromPath( getCurrentTemplatePath() ) & "artifacts/"
	toplevel="true"
	archive=""
	primary="physical"
	trusted="no";

admin action="compileMapping" type="server" password="#adminPwd#"
	virtual="/LDEV6078-debug"
	stoponerror="false";

// Find a compiled class file
classesDir = expandPath( "{lucee-server}/cfclasses" );
classFiles = directoryList( classesDir, true, "path", "*line_test*$cf.class" );

if ( classFiles.len() == 0 ) {
	classFiles = directoryList( classesDir, true, "path", "*line-test*$cf.class" );
}

systemOutput( "Found #classFiles.len()# matching class files", true );

if ( classFiles.len() > 0 ) {
	classFile = classFiles[ 1 ];
	systemOutput( "Analyzing: #classFile#", true );
	systemOutput( "", true );

	// Read and get bytecode text
	bytes = fileReadBinary( classFile );
	ClassReader = createObject( "java", "org.objectweb.asm.ClassReader" );
	TraceClassVisitor = createObject( "java", "org.objectweb.asm.util.TraceClassVisitor" );
	PrintWriter = createObject( "java", "java.io.PrintWriter" );
	StringWriter = createObject( "java", "java.io.StringWriter" );

	reader = ClassReader.init( bytes );
	sw = StringWriter.init();
	pw = PrintWriter.init( sw );
	visitor = TraceClassVisitor.init( javaCast( "null", "" ), pw );
	reader.accept( visitor, 0 );
	pw.flush();

	text = sw.toString();
	lines = listToArray( text, chr( 10 ) );

	// Show first 200 lines with line numbers
	systemOutput( "=== BYTECODE TEXT (first 200 lines) ===", true );
	for ( i = 1; i <= min( 200, lines.len() ); i++ ) {
		line = lines[ i ];
		// Highlight method signatures and LINENUMBER
		if ( findNoCase( "LINENUMBER", line ) ) {
			systemOutput( "#i#: >>> #line#", true );
		} else if ( reFindNoCase( "\)\s*[VZIJFDLSB\[]", line ) && !find( "=", line ) ) {
			// Method signature has return type descriptor
			systemOutput( "#i#: === METHOD === #line#", true );
		} else {
			systemOutput( "#i#: #line#", true );
		}
	}

	// Now run the scanner and show what it detects
	systemOutput( "", true );
	systemOutput( "=== SCANNER ANALYSIS ===", true );

	scanner = new BytecodeLineScanner();
	result = scanner.scanClassFile( classFile );

	systemOutput( "Methods detected:", true );
	methodsSeen = {};
	for ( entry in result.lineNumbers ) {
		if ( !methodsSeen.keyExists( entry.method ) ) {
			methodsSeen[ entry.method ] = 0;
		}
		methodsSeen[ entry.method ]++;
	}
	for ( m in methodsSeen ) {
		systemOutput( "  '#m#': #methodsSeen[ m ]# LINENUMBER entries", true );
	}

	systemOutput( "", true );
	systemOutput( "All LINENUMBER entries:", true );
	for ( entry in result.lineNumbers ) {
		systemOutput( "  method='#entry.method#' line=#entry.line# label=#entry.label#", true );
	}

	systemOutput( "", true );
	issues = scanner.findLineNumberIssues( result.lineNumbers );
	systemOutput( "Issues found: #issues.len()#", true );
	for ( issue in issues ) {
		systemOutput( "  #issue.description#", true );
	}
}

// Cleanup
try {
	admin action="removeMapping" type="server" password="#adminPwd#"
		virtual="/LDEV6078-debug";
} catch ( any e ) {}
</cfscript>
