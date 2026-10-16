component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll() {
		variables.uri = createURI( "LDEV6078" );
		variables.artifactsDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6078/artifacts/";
		variables.mappingName = "/LDEV6078-artifacts";

		// Known edge-case artifacts: these files have line number issues that are expected
		// due to complex expression structures or no-op statements.
		// When a statement generates no bytecode, it can't have its own line number.
		// Multiline expressions (especially ternary operators) spanning multiple lines have
		// inherent conflicts between statement and expression line numbers.
		// NOTE: Class files use underscores, source files use hyphens - include both patterns
		variables.knownNoOpArtifacts = [
			"multiline_comment_gap",      // var local = structNew() is a no-op
			"multiline-comment-gap",      // var local = structNew() is a no-op
			"multiline_return_expression", // ternary operators spanning lines have inherent conflicts
			"multiline-return-expression"  // ternary operators spanning lines have inherent conflicts
		];

		// Generate AST JSON files for each artifact
		generateAstFiles();
	}

	private void function generateAstFiles() {
		var artifacts = directoryList( variables.artifactsDir, false, "path", "*.cfm|*.cfc" );
		for ( var artifactPath in artifacts ) {
			var fileName = listLast( artifactPath, "/\" );
			var astFile = variables.artifactsDir & fileName & ".ast.json";
			try {
				var ast = astFromPath( artifactPath );
				fileWrite( astFile, serializeJSON( var=ast, compact=false ) );
			} catch ( any e ) {
				systemOutput( "AST generation failed for #fileName#: #e.message#", true );
			}
		}
	}

	function afterAll() {
		// Clean up mapping
		try {
			admin action="removeMapping" type="server" password="#request.SERVERADMINPASSWORD#"
				virtual=variables.mappingName;
		} catch ( any e ) {}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6078: SourceCode.getPosition() line number bug", function() {

			it( "bytecode should have correct line numbers for consecutive statements", function() {
				// Create mapping for artifacts
				admin action="updateMapping" type="server" password="#request.SERVERADMINPASSWORD#"
					virtual=variables.mappingName
					physical=variables.artifactsDir
					toplevel="true"
					archive=""
					primary="physical"
					trusted="no";

				// Compile the mapping to generate bytecode
				var compileErrors = {};
				admin action="compileMapping" type="server" password="#request.SERVERADMINPASSWORD#"
					virtual=variables.mappingName
					stoponerror="false"
					errorVariable="compileErrors";

				if ( structCount( compileErrors ) > 0 ) {
					for ( var key in compileErrors ) {
						systemOutput( "Compile error: #key#: #serializeJSON( compileErrors[ key ] )#", true );
					}
				}

				// Find compiled class files
				var classesDir = expandPath( "{lucee-server}/cfclasses" );
				expect( directoryExists( classesDir ) ).toBeTrue( "Classes directory should exist: #classesDir#" );

				var classFiles = directoryList( classesDir, true, "path", "*.class" );

				// Filter to class files from our test artifacts mapping
				var testClasses = classFiles.filter( function( f ) {
					return findNoCase( "LDEV6078", f ) && findNoCase( "$cf.class", f );
				});

				systemOutput( "Found #testClasses.len()# test class files to scan", true );

				// Scan bytecode using ASM
				var scanner = new LDEV6078.BytecodeLineScanner();
				var allIssues = [];

				for ( var classFile in testClasses ) {
					var scanResult = scanner.scanClassFile( classFile );
					var fileName = listLast( classFile, "/\" );

					// Check if this is a known no-op artifact (expected to have shared labels)
					var isKnownNoOp = false;
					for ( var noOpName in variables.knownNoOpArtifacts ) {
						if ( findNoCase( noOpName, fileName ) ) {
							isKnownNoOp = true;
							break;
						}
					}

					systemOutput( "Scanned #fileName##isKnownNoOp ? ' (known no-op)' : ''#: #scanResult.exeLogCalls.len()# exeLogStart, #scanResult.lineNumbers.len()# LINENUMBER entries", true );

					// Check exeLogStart calls if present (debugger mode)
					if ( scanResult.exeLogCalls.len() > 0 ) {
						var issues = findDuplicateLineIssues( scanResult.exeLogCalls );
						if ( issues.len() > 0 ) {
							for ( var issue in issues ) {
								if ( isKnownNoOp ) {
									systemOutput( "  (expected no-op issue) #issue.description#", true );
								} else {
									allIssues.append( fileName & " (exeLog): " & issue.description );
								}
							}
						}
					}

					// Always check JVM LineNumberTable (always present)
					if ( scanResult.lineNumbers.len() > 0 ) {
						// Debug: show ALL line numbers per method (with labels to see duplicates)
						var byMethod = {};
						for ( var entry in scanResult.lineNumbers ) {
							if ( !byMethod.keyExists( entry.method ) ) byMethod[ entry.method ] = [];
							byMethod[ entry.method ].append( "#entry.line#(#entry.label#)" );
						}
						for ( var m in byMethod ) {
							systemOutput( "  #m#: [#byMethod[ m ].toList()#]", true );
						}

						var lineIssues = scanner.findLineNumberIssues( scanResult.lineNumbers );
						if ( lineIssues.len() > 0 ) {
							for ( var issue in lineIssues ) {
								if ( isKnownNoOp ) {
									systemOutput( "  (expected no-op issue) #issue.description#", true );
								} else {
									allIssues.append( fileName & " (LineNumberTable): " & issue.description );
								}
							}
						}
					}
				}

				// Report all issues found
				if ( allIssues.len() > 0 ) {
					systemOutput( "", true );
					systemOutput( "!!! LINE NUMBER ISSUES FOUND !!!", true );
					for ( var issue in allIssues ) {
						systemOutput( "  #issue#", true );
					}
					fail( "Found #allIssues.len()# bytecode line number issues - see output for details" );
				}
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

	/**
	 * Find duplicate consecutive line issues (symptoms of the bug)
	 * Same logic as BytecodeLineScanner.findDuplicateLineIssues
	 */
	private array function findDuplicateLineIssues( required array calls ) {
		var issues = [];

		// Group by method
		var byMethod = {};
		for ( var call in calls ) {
			if ( !byMethod.keyExists( call.method ) ) {
				byMethod[ call.method ] = [];
			}
			byMethod[ call.method ].append( call );
		}

		// Check each method for consecutive same-line calls
		for ( var methodName in byMethod ) {
			var methodCalls = byMethod[ methodName ];
			for ( var i = 2; i <= methodCalls.len(); i++ ) {
				var prev = methodCalls[ i - 1 ];
				var curr = methodCalls[ i ];

				// Same line number for different statements (different ids)
				if ( prev.line == curr.line && prev.id != curr.id ) {
					issues.append( {
						method: methodName,
						line: curr.line,
						ids: [ prev.id, curr.id ],
						description: "Line #curr.line# has multiple statements (ids: #prev.id#, #curr.id#) in method #methodName#"
					} );
				}
			}
		}

		return issues;
	}

}
