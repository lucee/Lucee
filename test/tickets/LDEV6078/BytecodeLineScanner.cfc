/**
 * Scan compiled CFML bytecode for exeLogStart line numbers using ASM
 * Used to verify correct line numbers in compiled bytecode for debugger support
 */
component {

	/**
	 * Scan a directory of compiled classes and extract exeLogStart line numbers
	 * @classDir Directory containing .class files (typically lucee-server/context/classes)
	 * @return struct with files scanned and line number data
	 */
	public struct function scanDirectory( required string classDir ) {
		var result = {
			scannedFiles: 0,
			filesWithExeLog: 0,
			totalExeLogCalls: 0,
			duplicateLineIssues: [],
			files: {}
		};

		if ( !directoryExists( classDir ) ) {
			result.error = "Directory not found: #classDir#";
			return result;
		}

		var classFiles = directoryList( classDir, true, "path", "*.class" );
		var totalFiles = classFiles.len();
		systemOutput( "Found #totalFiles# class files to scan", true );

		for ( var classFile in classFiles ) {
			result.scannedFiles++;

			// Progress every 500 files
			if ( result.scannedFiles mod 500 == 0 ) {
				systemOutput( "  Progress: #result.scannedFiles#/#totalFiles# files scanned...", true );
			}

			var fileResult = scanClassFile( classFile );
			if ( fileResult.exeLogCalls.len() > 0 ) {
				result.filesWithExeLog++;
				result.totalExeLogCalls += fileResult.exeLogCalls.len();

				// Check for duplicate consecutive lines (the bug symptom)
				var issues = findDuplicateLineIssues( fileResult.exeLogCalls );
				if ( issues.len() > 0 ) {
					result.duplicateLineIssues.append( {
						file: classFile,
						issues: issues
					} );
				}

				// Store per-file data
				var relPath = listLast( classFile, "/\" );
				result.files[ relPath ] = fileResult;
			}
		}

		return result;
	}

	/**
	 * Scan a single class file for exeLogStart calls and JVM line numbers
	 * @classFile Path to .class file
	 * @return struct with exeLogCalls array and lineNumbers array
	 */
	public struct function scanClassFile( required string classFile ) {
		var bytes = fileReadBinary( classFile );
		var text = getBytecodeText( bytes );

		return {
			exeLogCalls: extractExeLogCalls( text ),
			lineNumbers: extractLineNumbers( text ),
			className: extractClassName( text )
		};
	}

	/**
	 * Extract JVM LineNumberTable entries from bytecode text
	 * These are always present and show: LINENUMBER <line> <label>
	 * @return array of structs with {line, method, label}
	 */
	public array function extractLineNumbers( required string text ) {
		var entries = [];
		var lines = listToArray( text, chr( 10 ) );
		var currentMethod = "";

		for ( var i = 1; i <= lines.len(); i++ ) {
			var line = trim( lines[ i ] );

			// Track current method
			if ( reFindNoCase( "^\s*(public|private|protected|static|final|synchronized|native|abstract|\s)*\s*\w+\s*\(", lines[ i ] )
				&& find( ")", lines[ i ] ) && !find( "=", lines[ i ] ) ) {
				currentMethod = extractMethodName( lines[ i ] );
			}

			// Look for LINENUMBER entries: LINENUMBER 12 L0
			if ( reFindNoCase( "^\s*LINENUMBER\s+(\d+)\s+(\w+)", line ) ) {
				var match = reFindNoCase( "LINENUMBER\s+(\d+)\s+(\w+)", line, 1, true );
				if ( match.len.len() > 2 ) {
					var lineNum = int( mid( line, match.pos[ 2 ], match.len[ 2 ] ) );
					var label = mid( line, match.pos[ 3 ], match.len[ 3 ] );
					entries.append( {
						line: lineNum,
						label: label,
						method: currentMethod,
						bytecodeIndex: i
					} );
				}
			}
		}

		return entries;
	}

	/**
	 * Find issues in JVM LineNumberTable:
	 * 1. Same label with different line numbers (BUG - single bytecode location can't be on multiple lines)
	 * 2. Same line with different labels (may be normal for multi-statement lines)
	 */
	public array function findLineNumberIssues( required array lineNumbers ) {
		var issues = [];

		// Group by method
		var byMethod = {};
		for ( var entry in lineNumbers ) {
			if ( !byMethod.keyExists( entry.method ) ) {
				byMethod[ entry.method ] = [];
			}
			byMethod[ entry.method ].append( entry );
		}

		for ( var methodName in byMethod ) {
			var methodEntries = byMethod[ methodName ];

			// Track labels we've seen and their line numbers
			var labelToLine = {};

			for ( var entry in methodEntries ) {
				// Check for same label with different line numbers (the bug!)
				if ( labelToLine.keyExists( entry.label ) ) {
					var prevLine = labelToLine[ entry.label ];
					if ( prevLine != entry.line ) {
						issues.append( {
							method: methodName,
							label: entry.label,
							lines: [ prevLine, entry.line ],
							description: "Label #entry.label# assigned to both line #prevLine# and line #entry.line# in #methodName#"
						} );
					}
				}
				labelToLine[ entry.label ] = entry.line;
			}
		}

		return issues;
	}

	/**
	 * Get bytecode text representation using ASM's TraceClassVisitor
	 */
	private string function getBytecodeText( required binary classBytes ) {
		var ClassReader = createObject( "java", "org.objectweb.asm.ClassReader" );
		var TraceClassVisitor = createObject( "java", "org.objectweb.asm.util.TraceClassVisitor" );
		var PrintWriter = createObject( "java", "java.io.PrintWriter" );
		var StringWriter = createObject( "java", "java.io.StringWriter" );

		var reader = ClassReader.init( classBytes );
		var sw = StringWriter.init();
		var pw = PrintWriter.init( sw );

		// TraceClassVisitor with null delegate and PrintWriter
		var visitor = TraceClassVisitor.init( javaCast( "null", "" ), pw );

		// Don't skip debug info - we want line numbers
		reader.accept( visitor, 0 );
		pw.flush();

		return sw.toString();
	}

	/**
	 * Extract class name from bytecode text
	 */
	private string function extractClassName( required string text ) {
		var match = reFind( "class\s+([^\s]+)", text, 1, true );
		if ( match.len.len() > 1 && match.len[ 2 ] > 0 ) {
			return mid( text, match.pos[ 2 ], match.len[ 2 ] );
		}
		return "unknown";
	}

	/**
	 * Extract exeLogStart calls from bytecode text
	 * Looks for patterns like:
	 *   BIPUSH/SIPUSH/LDC <line>
	 *   LDC "<id>"
	 *   INVOKEVIRTUAL ... exeLogStart
	 *
	 * @return array of structs with {line, id, method}
	 */
	private array function extractExeLogCalls( required string text ) {
		var calls = [];
		var lines = listToArray( text, chr( 10 ) );
		var currentMethod = "";
		var recentInts = [];  // Track recent integer pushes

		for ( var i = 1; i <= lines.len(); i++ ) {
			var line = trim( lines[ i ] );

			// Track current method
			if ( reFindNoCase( "^\s*(public|private|protected|static|final|synchronized|native|abstract|\s)*\s*\w+\s*\(", lines[ i ] )
				&& find( ")", lines[ i ] ) && !find( "=", lines[ i ] ) ) {
				currentMethod = extractMethodName( lines[ i ] );
				recentInts = [];
			}

			// Track integer constants being pushed onto stack
			// BIPUSH for -128 to 127, SIPUSH for larger, ICONST_x for 0-5, LDC for others
			if ( reFindNoCase( "^\s*(BIPUSH|SIPUSH)\s+(-?\d+)", line ) ) {
				var match = reFindNoCase( "(BIPUSH|SIPUSH)\s+(-?\d+)", line, 1, true );
				if ( match.len.len() > 2 ) {
					recentInts.append( int( mid( line, match.pos[ 3 ], match.len[ 3 ] ) ) );
				}
			} else if ( reFindNoCase( "^\s*ICONST_(\d)", line ) ) {
				var match = reFindNoCase( "ICONST_(\d)", line, 1, true );
				if ( match.len.len() > 1 ) {
					recentInts.append( int( mid( line, match.pos[ 2 ], match.len[ 2 ] ) ) );
				}
			} else if ( reFindNoCase( "^\s*LDC\s+(-?\d+)\s*$", line ) ) {
				var match = reFindNoCase( "LDC\s+(-?\d+)", line, 1, true );
				if ( match.len.len() > 1 ) {
					recentInts.append( int( mid( line, match.pos[ 2 ], match.len[ 2 ] ) ) );
				}
			}

			// Look for exeLogStart invocation
			if ( findNoCase( "exeLogStart", line ) && findNoCase( "INVOKE", line ) ) {
				// The line number should be the most recent integer pushed
				var lineNum = recentInts.len() > 0 ? recentInts[ recentInts.len() ] : -1;

				// Try to get the id string (usually pushed just before the int or after)
				var id = extractRecentStringId( lines, i );

				calls.append( {
					line: lineNum,
					id: id,
					method: currentMethod,
					bytecodeIndex: i
				} );

				recentInts = [];
			}

			// Clear int tracking on other invokes (not our target)
			if ( findNoCase( "INVOKE", line ) && !findNoCase( "exeLogStart", line ) ) {
				recentInts = [];
			}
		}

		return calls;
	}

	/**
	 * Extract method name from declaration line
	 */
	private string function extractMethodName( required string line ) {
		var match = reFind( "(\w+)\s*\([^\)]*\)", line, 1, true );
		if ( match.len.len() > 0 && match.len[ 1 ] > 0 ) {
			return mid( line, match.pos[ 1 ], match.len[ 1 ] );
		}
		return "unknown";
	}

	/**
	 * Look backwards from current position to find LDC "string" for the id
	 */
	private string function extractRecentStringId( required array lines, required numeric currentIndex ) {
		// Look back up to 5 lines for a string constant
		var start = max( 1, currentIndex - 5 );
		for ( var i = currentIndex - 1; i >= start; i-- ) {
			var line = lines[ i ];
			// LDC "something"
			var match = reFind( 'LDC\s+"([^"]+)"', line, 1, true );
			if ( match.len.len() > 1 && match.len[ 2 ] > 0 ) {
				return mid( line, match.pos[ 2 ], match.len[ 2 ] );
			}
		}
		return "";
	}

	/**
	 * Find duplicate consecutive line issues (symptoms of the bug)
	 * @calls Array of exeLogStart calls
	 * @return Array of issues found
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
						description: "Two different statements report same line " & curr.line & " (ids: " & prev.id & ", " & curr.id & ")"
					} );
				}
			}
		}

		return issues;
	}

	/**
	 * Pretty print scan results
	 */
	public void function printResults( required struct result ) {
		systemOutput( "", true );
		systemOutput( "=== Bytecode Line Number Scan Results ===", true );
		systemOutput( "Files scanned: #result.scannedFiles#", true );
		systemOutput( "Files with exeLogStart: #result.filesWithExeLog#", true );
		systemOutput( "Total exeLogStart calls: #result.totalExeLogCalls#", true );
		systemOutput( "", true );

		if ( result.duplicateLineIssues.len() > 0 ) {
			systemOutput( "!!! DUPLICATE LINE ISSUES FOUND: #result.duplicateLineIssues.len()# files", true );
			for ( var fileIssue in result.duplicateLineIssues ) {
				systemOutput( "  File: #fileIssue.file#", true );
				for ( var issue in fileIssue.issues ) {
					systemOutput( "    - #issue.description#", true );
				}
			}
		} else {
			systemOutput( "No duplicate line issues found", true );
		}
	}

}
