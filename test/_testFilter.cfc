component {
	public function init (){
		return this;
	}

	/* manually filter test suite based on naming conventions, labels, skip, etc */
	public boolean function filterTest ( required string path, required string testDirectory, required string testMapping ) localmode=true {
		//systemOutput(arguments, true);
		var isValidTestCase = function ( required string path ){
			// get parent
			var testDir = getDirectoryFromPath( arguments.path );
			testDir = listCompact( left( testDir, testDir.len() - 1 ), "\/" );
			if ( left( arguments.path, 1 ) eq "/")
				testDir = "/" & testDir; // avoid issues with non windows paths
			var name = listLast( arguments.path, "\/" );
			var testPath = Mid( arguments.path, len( testDirectory ) + 1); // otherwise "image" would match extension-image on CI
			switch ( true ){
				case ( left( name, 1 ) == "_" && request.testSkip ):
					return "test has _ prefix (#name#)";
				case ( checkTestFilter( testPath ) ):
					return "excluded by testFilter";
				case ( FindNoCase( testDirectory, testDir ) neq 1 ):
					return "not under test dir (#testDirectory#, #testDir#)";
				case fileExists( testDir & "/Application.cfc" ):
					var meta = getTestMeta( arguments.path, true );
					var isTest = checkExtendsTestCase( meta, arguments.path );
					if ( len( isTest ) ){
						return isTest;
					} else {
						return "Test in directory with Application.cfc";
					}
				default:
					break;
			};
			var meta = getTestMeta( arguments.path );

			if (request.testSkip && structKeyExists(meta, "skip") && meta.skip ?: false)
				return "test suite has skip=true";

			var extends = checkExtendsTestCase( meta, arguments.path ); // returns an empty string, unless error
			if ( len( extends ) )
				return extends;
			
			var labelCheck =  checkTestLabels( meta, arguments.path, request.testLabels );
			if ( len( labelCheck ) )
				return labelCheck;

			// only report an exception if all other filters have passed
			if ( structKeyExists( meta, "_exception" ) ) {
				if ( request.testDebug ){
					if ( !meta.skip && request.testDebug ) {
						printCompileException( arguments.path, meta._exception );
					} 
				} else { //} if ( !request.testSkip ){
					// throw an error on bad cfc test cases
					// but ignore errors when using any labels, as some extensions might not be installed, causing compile syntax errors
					printCompileException( arguments.path, meta._exception );
					throw( object=meta._exception );
				}
				return meta._exception.message;
			}
			return "";
		};

		var allowed = isValidTestCase( arguments.path );
		//SystemOutput( arguments.path & " :: " & allowed, true );
		if ( allowed != "" ){
			if ( request.testDebug )
				SystemOutput( arguments.path & " :: " & allowed, true );
			return false;
		} else {
			return true;
		}
	}

	public boolean function checkTestFilter ( required string path ){
		if ( Arraylen( request.testFilter ) eq 0 )
			return false;
		loop array="#request.testFilter#" item="local.filter" {
			if ( FindNoCase( filter, arguments.path ) gt 0 )
				return false;
		}
		return true;
	}

	public string function checkExtendsTestCase ( required struct meta, required string path ){
		var extends = arguments.meta.extends.fullname ?: "";
		if ( extends eq "Lucee.component" or len( extends ) eq 0 ) {
			return 'Not a test suite [#(arguments.meta.extends?:{}).toJson()#]'; // plain old cfc, ignore
		} else if ( listFindNoCase( request.testSuiteExtends, extends ) eq 0 ) {
			// default is "org.lucee.cfml.test.LuceeTestCase"
			return "Test extends wrong Base spec [#extends#] "
				& "see ' -dtestSuiteExtends=""cfc.path"" ' ";
		} else {
			return "";
		}
	}

	public struct function getTestMeta ( required string path ){
		// finally only allow files which extend "org.lucee.cfml.test.LuceeTestCase"
		var cfcPath = ListChangeDelims( testMapping & Mid( arguments.path, len( testDirectory ) + 1 ), ".", "/\" );
		cfcPath = mid( cfcPath, 1, len( cfcPath ) - 4 ); // strip off ".cfc"

		if ( fileRead( arguments.path ) does not contain "extends" )
			return { skip: true, missingExtends: true };
		try {
			// triggers a compile, which make the initial filter slower, but it would be compiled later anyway
			// GetComponentMetaData leaks output https://luceeserver.atlassian.net/browse/LDEV-3582
			silent {
				var meta = GetComponentMetaData( cfcPath );
			}
		} catch ( e ) {
			// try and manually parse to see if there's a skip="true", if not throw
			// TODO refactor, need to respect test labels (thus possible ignore)
			var meta = sniffMetaDataFromBrokenCFC( arguments.path );
			meta['_exception'] = e;
		}
		return meta;
	}

	public function printCompileException( string cfcPath="", required struct cfcatch ){
		systemOutput( "ERROR: #arguments.cfcPath#", true );
		systemOutput( chr(9) & arguments.cfcatch.message, true );
		if ( !isEmpty( arguments.cfcatch.tagContext ) ){
			systemOutput( chr( 9 ) & "at line: " 
				& ( arguments.cfcatch.tagContext[1].line ?: "unknown")
				& ", column: " & ( arguments.cfcatch.tagContext[1].column ?: "unknown")
				, true 
			);
			systemOutput( arguments.cfcatch.tagContext[1].codePrintPlain, true );
		}
	}

	// testbox mixes labels and skip, which is confusing, skip false should always mean skip, so we check it manually
	public string function checkTestLabels ( required struct meta, required string path, required array requiredTestLabels ){
		if ( arrayLen ( arguments.requiredTestLabels ) eq 0 )
			return ""; // no labels to filter by
		var testLabels = meta.labels ?: "";
		var labelsMatched = [];

		// TODO allow any of syntax, orm|cache
		loop array="#arguments.requiredTestLabels#" item="local.label" {
			if ( ListFindNoCase( testLabels, label ) gt 0 )
				ArrayAppend( labelsMatched, label );
		}
		var matched = false;

		if ( ArrayLen( labelsMatched ) eq arrayLen( arguments.requiredTestLabels ) )
			matched = true; // matched all the required labels
		if ( matched and listLen( testLabels ) neq ArrayLen( labelsMatched ) )
			matched = false; // but we didn't match all the specified labels for the test

		var matchStatus = "#arguments.path# [#testLabels#] matched required label(s) #serializeJson(arguments.requiredTestLabels)#,"
			& " only #serializeJson( labelsMatched )# matched";
		if ( !matched ){
			// systemOutput( "FAILED: " & matchStatus, true );
			return matchStatus;
		} else {
			// systemOutput( "OK: " & matchStatus  , true);
			return ""; //ok
		}
	}

	// when a CFC won't compile, try and manually parse  meta data
	public struct function sniffMetaDataFromBrokenCFC ( required string cfcPath ) {

		local.meta = {
			skip: false,
			extends: "",
			labels: ""
		};

		local.src = fileRead( arguments.cfcPath );
		src = reReplace(src, "<!---.*?--->", "", "all"); // strip out cfml comments
		src = reReplace(src, "/\/\*[\s\S]*?\*\/|([^:]|^)\/\/.*$", "", "all"); // strip out script comments
		src = trim(src);

		local.isCfml = find( "<" & "cfcomponent", src );
		local.isScript = find( "component", src );

		if ( isCfml == 0 && isScript == 0 ){
			throw "bad cfc [#arguments.cfcPath#], can't even find a component tag / statement";
		} else if ( isCfml gt 0 ){
			local.endStatement = find( ">", src, isCfml );
		} else { // isScript
			local.endStatement = find( "{", src, isScript );
		}

		if ( endStatement eq 0 ){
			systemOutput(local, true);
			systemOutput(left(src, 200), true);
			throw "bad cfc [#arguments.cfcPath#], no closing statement";
		} 

		local.snip = mid(src, ((isCfml > 0) ? isCfml : isScript), endStatement);
		//systemOutput(local, true);

		// first try creating a stub cfc and extracting the component metadata
		try {
			local.meta = extractMetadataFromStub( snip, ( isCfml > 0 ) );
			if ( isNull( meta.skip ) )
				meta.skip = false;
			return meta;
		} catch (e){
			// ignore, try manual parsing
		}

		meta.manualParsing=true;

		//systemOutput(local.snip, true);
		// so far, all we care about if finding a skip directive?
		local.hasSkip = find( "skip", snip );
		if ( hasSkip gt 0 ) {
			//systemOutput(local.hasSkip, true);
			// hacky but all we are looking for is a skip true
			local.hasTrue = find( "true", snip, local.hasSkip );
			// systemOutput(local.hasTrue, true);
			//systemOutput(local, true);
			if ( hasTrue gt 0 and hasTrue lt (local.hasSkip + 7) )
				meta.skip = true;
		}
		return meta
	}

	// create a stub cfc to try and extract out just the component metadata (from a cfc with compile errors)
	private struct function extractMetadataFromStub( required string str, required boolean isCFml ){
		if ( arguments.isCFml ){
			local.src = arguments.str & '</c' & 'fcomponent">';
		} else {
			local.src = arguments.str & ' this.stubCFC=true; }';
		}
		// systemOutput( "SRC:" & str, true );
		// systemOutput( "SRC:" & src, true );
		local.tempMapping = expandPath( "/test" ) & "/tempCFC/";  // TODO this could be a mapping under the temp directory
		if ( !directoryExists( tempMapping ) )
			directoryCreate( tempMapping )
		local.tempCFC = getTempFile( tempMapping, "tempCFC", "cfc" );
		fileWrite( tempCFC, src );
		try {
			silent {
				local.meta = getComponentMetadata( "/test/tempCFC/" & listFirst( listLast( tempCFC, "/\" ), "." ) );
			}
		} catch(e){
			// systemOutput(e, true);
			fileDelete( tempCFC );
			rethrow;	
		};
		fileDelete( tempCFC );
		// systemOutput( meta, true );
		return meta;
	}

}
