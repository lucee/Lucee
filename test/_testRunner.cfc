component {
	public function init (){
		return this;
	}

	// testbox doesn't always sort the order of tests, so we do it manually LDEV-3541
	public array function getBundles( testMapping, testDirectory ){
		var srcBundles = directoryList( path="#expandPath(arguments.testMapping)#", recurse=true, listInfo="path", filter="*.cfc" );
		var testDirectoryLen = len( arguments.testDirectory );
		var mapping = ListChangeDelims( arguments.testMapping, "", "/\" );
		var bundles = [];
		ArrayEach( array=srcBundles, closure=function( el, idx, arr ){
			if ( testFilter( arguments.el, testDirectory, testMapping ) ) {
				var clean  = ListChangeDelims( mid( arguments.el, testDirectoryLen + 1  ), ".", "/\" ); // strip off dir prefix
				arrayAppend(bundles, mapping & "." & mid( clean, 1, len( clean ) - 4 ) ); // strip off .cfc
			}
		}, parallel=true );
		ArraySort( bundles, "textnocase", "asc" );
		return bundles;
	}

	public boolean function testFilter ( string path, string testDirectory, string testMapping ) localmode=true {
		//systemOutput(arguments, true);
		var isValidTestCase = function ( string path ){
			// get parent
			var testDir = getDirectoryFromPath( arguments.path );
			testDir = listCompact( left( testDir, testDir.len() - 1 ), "\/" );
			if ( left( arguments.path, 1 ) eq "/")
				testDir = "/" & testDir; // avoid issues with non windows paths
			var name = listLast( arguments.path, "\/" );
			var testPath = Mid( arguments.path, len( testDirectory ) + 1); // otherwise "image" would match extension-image on CI
			switch ( true ){
				case ( left (name, 1 ) == "_" && request.testSkip):
					return "test has _ prefix (#name#)";
				case ( checkTestFilter( testPath ) ):
					return "excluded by testFilter";
				case ( FindNoCase( testDirectory, testDir ) neq 1 ):
					return "not under test dir (#testDirectory#, #testDir#)";
				case fileExists( testDir & "/Application.cfc" ):
					return "test in directory with Application.cfc";
				default:
					break;
			};
			var meta = getTestMeta( arguments.path );
			if ( structKeyExists( meta, "_exception" ) ) {
				if ( request.testDebug ){
					SystemOutput( "ERROR: [" & arguments.path & "] threw " & meta._exception.message, true );
				} else { //} if ( !request.testSkip ){
					if ( fileRead( arguments.path ) contains "org.lucee.cfml.test.LuceeTestCase" ){
						// throw an error on bad cfc test cases
						// but ignore errors when using any labels, as some extensions might not be installed, causing compile syntax errors
						if ( len( request.testLabels ) eq 0 ) {
							SystemOutput( "ERROR: [" & arguments.path & "] threw " & meta._exception.message, true );
							throw( object=meta._exception );
						}
					}
				}
				return meta._exception.message;
			}

			if (request.testSkip && structKeyExists(meta, "skip") && meta.skip ?: false)
				return "test suite has skip=true";

			var extends = checkExtendsTestCase( meta, arguments.path );
			if ( extends neq "org.lucee.cfml.test.LuceeTestCase" )
				return "test doesn't extend Lucee Test Case (#extends#)";

			return checkTestLabels( meta, arguments.path, request.testLabels );
		};

		var checkTestFilter = function ( string path ){
			if ( Arraylen( request.testFilter ) eq 0 )
				return false;
			loop array="#request.testFilter#" item="local.f" {
				if ( FindNoCase( f, arguments.path ) gt 0 )
					return false;
			}
			return true;
		};

		var getTestMeta = function (string path){
			// finally only allow files which extend "org.lucee.cfml.test.LuceeTestCase"
			var cfcPath = ListChangeDelims( testMapping & Mid( arguments.path, len( testDirectory ) + 1 ), ".", "/\" );
			cfcPath = mid( cfcPath, 1, len( cfcPath ) - 4 ); // strip off ".cfc"
			try {
				// triggers a compile, which make the initial filter slower, but it would be compiled later anyway
				// GetComponentMetaData leaks output https://luceeserver.atlassian.net/browse/LDEV-3582
				silent {
					var meta = GetComponentMetaData( cfcPath );
				}
			} catch ( e ){
				if ( request.testDebug )
					systemOutput( cfcatch, true );
				return {
					"_exception": cfcatch
				}
			}
			return meta;
		};

		var checkExtendsTestCase = function (any meta, string path){
			return meta.extends.fullname ?: "";
		};

		/* testbox mixes labels and skip, which is confusing, skip false should always mean skip, so we check it manually */
		var checkTestLabels = function ( required any meta, required string path, required array requiredTestLabels ){
			if ( arrayLen ( arguments.requiredTestLabels ) eq 0 )
				return ""; // no labels to filter by
			var testLabels = meta.labels ?: "";
			var labelsMatched = [];

			// TODO allow any of syntax, orm|cache
			loop array="#arguments.requiredTestLabels#" item="local.f" {
				if ( ListFindNoCase( testLabels, f ) gt 0 )
					ArrayAppend( labelsMatched, f );
			}
			var matched = false;

			if ( ArrayLen( labelsMatched ) eq arrayLen( arguments.requiredTestLabels ) )
				matched = true; // matched all the required labels
			if ( matched and listLen( testLabels ) neq ArrayLen( labelsMatched ) )
				matched = false; // but we didn't match all the specified labels for the test

			var matchStatus = "#path# [#testLabels#] matched required label(s) #serializeJson(arguments.requiredTestLabels)#,"
				& " only #serializeJson( labelsMatched )# matched";
			if ( !matched ){
				// systemOutput( "FAILED: " & matchStatus, true );
				return matchStatus;
			} else {
				// systemOutput( "OK: " & matchStatus  , true);
				return ""; //ok
			}
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

	public struct function runTests() localmode=true {
		SystemOut = createObject( "java", "lucee.commons.lang.SystemOut" );
		out = SystemOut.setOut( nullValue() );
		//err=SystemOut.setErr(nullValue());
		TAB = chr( 9 );
		NL = chr( 10 ) & chr( 13 );
		failedTestCases = [];

		// strips off the stack trace to exclude testbox and back to the first .cfc call in the stack
		function printStackTrace( st ){
			local.i = find( "/testbox/", arguments.st );
			if ( request.testDebug || i eq 0 ){ // dump it all out
				systemOutput( TAB & arguments.st, true );
				return;
			}
			local.tmp = mid( arguments.st, 1, i ); // strip out anything after testbox
			local.tmp2 = reverse( local.tmp );
			local.i = find( ":cfc.", local.tmp2 ); // find the first cfc line
			if ( local.i gt 0 ){
				local.i = len( local.tmp )-i;
				local.j = find( ")", local.tmp, local.i ); // find the end of the line
				local.tmp = mid( local.tmp, 1, local.j );
			}
			systemOutput( TAB & local.tmp, true );
		};

		try {
			var filterTimer = getTickCount();
			var bundles = getBundles( "/test", request.testFolder );
			//SystemOutput( bundles, true);
			var additionalBundles = [];
			if ( len( request.testAdditional ) ){
				additionalBundles = getBundles( "/testAdditional", request.testAdditional );
				// SystemOutput( additionalBundles, true );
				bundles = ArrayMerge( bundles, additionalBundles );
			}
			var tb = new testbox.system.TestBox( bundles=bundles, reporter="console" );

			SystemOutput( "Found #tb.getBundles().len()# tests to run, filter took #getTickCount()-filterTimer#ms", true );
			if ( len( additionalBundles ) ){
				SystemOutput( "Found #additionalBundles.len()# additional tests to run", true );
			}
			if (false and Arraylen( request.testFilter )){
				// dump matches by testFilter
				for ( b in tb.getBundles() )
					SystemOutput( b, true );
			}
			// formatting is odd, because we are outputting to the console and whitespace matters
			// execute
			tb.run(callbacks=
{
	 onBundleStart = function( cfc, testResults ){
		var meta = getComponentMetadata( cfc );
		SystemOut.setOut( out );
		//SystemOut.setErr(err);
		//"============================================================="
		systemOutput( TAB & meta.name & " ", false );
		SystemOut.setOut( nullValue() );
		//SystemOut.setErr(nullValue());
	} // onBundleStart = function
	,onBundleEnd = function( cfc, testResults ){
		var bundle = arrayLast( testResults.getBundleStats() );
		var oh = ( getTickCount() - request._tick )-bundle.totalDuration;
		request._tick = getTickCount();
		ArrayAppend( request.overhead, oh );
		try {
			SystemOut.setOut( out );
			//SystemOut.setErr(err);
			systemOutput( TAB & " (#bundle.totalPass# tests passed in #NumberFormat(bundle.totalDuration)# ms)", true );
			//mem("non_heap");
			//mem("heap");
		// we have an error
		if ( ( bundle.totalFail + bundle.totalError ) > 0 ) {

			systemOutput( "ERRORED" & NL & "	Suites/Specs: #bundle.totalSuites#/#bundle.totalSpecs#
	Failures: #bundle.totalFail#
	Errors:   #bundle.totalError#
	Pass:     #bundle.totalPass#
	Skipped:  #bundle.totalSkipped#"
			, true );
			
			if ( !isNull( bundle.suiteStats ) ) {
				loop array=bundle.suiteStats item="local.suiteStat" {
					local.specStats = duplicate(suiteStat.specStats);
					// spec stats are also nested 
					loop array=suiteStat.suiteStats item="local.nestedSuiteStats" {
						if ( !isEmpty( local.nestedSuiteStats.specStats ) ) {
							loop array=local.nestedSuiteStats.specStats item="local.nestedSpecStats" {
								arrayAppend( local.specStats, local.nestedspecStats );
							}
						}
					}

					if ( isEmpty( local.specStats ) ) {
						systemOutput( "WARNING: suiteStat for [#bundle.name#] was empty?", true );
					} else {
						loop array=local.specStats item="local.specStat" {
							if ( !isNull( specStat.failMessage ) && len( trim( specStat.failMessage ) ) ) {

								var failedTestCase = {
									 type       : "Failed"
									,bundle     : bundle.name
									,testCase   : specStat.name
									,errMessage : specStat.failMessage
									,stackTrace : []
								};
								failedTestCases.append( failedTestCase );

								systemOutput( NL & specStat.name );
								systemOutput( NL & TAB & "Failed: " & specStat.failMessage, true );

								if ( !isNull( specStat.failOrigin ) && !isEmpty( specStat.failOrigin ) ){

									var rawStackTrace = specStat.failOrigin;
									var testboxPath = getDirectoryFromPath( rawStackTrace[1].template );

									//systemOutput(TAB & TAB & "at", true);

									loop array=rawStackTrace item="local.st" index="local.i" {

										if ( !st.template.hasPrefix( testboxPath ) ){
											if ( local.i eq 1 or st.template does not contain "testbox" ){
												var frame = st.template & ":" & st.line;
												failedTestCase.stackTrace.append( frame );
												systemOutput( TAB & frame, true );
											}
										}
									}
								}
								systemOutput( NL );
							} // if !isNull

							if ( !isNull( specStat.error ) && !isEmpty( specStat.error ) ){

								var failedTestCase = {
									 type       : "Errored"
									,bundle     : bundle.name
									,testCase   : specStat.name
									,errMessage : specStat.error.Message
									,stackTrace : []
								};
								failedTestCases.append( failedTestCase );

								systemOutput( NL & specStat.name );
								systemOutput( NL & TAB & "Errored: " & specStat.error.Message, true );
								if ( len( specStat.error.Detail ) )
									systemOutput( TAB & "Detail: " & specStat.error.Detail, true );

								if ( !isNull( specStat.error.TagContext ) && !isEmpty( specStat.error.TagContext ) ){

									var rawStackTrace = specStat.error.TagContext;

									//systemOutput(TAB & TAB & "at", true);

									loop array=rawStackTrace item="local.st" index="local.i" {
										if ( local.i eq 1 or st.template does not contain "testbox" ){
											var frame = st.template & ":" & st.line;
											failedTestCase.stackTrace.append( frame );
											systemOutput( TAB & frame, true );
										}
									}
									systemOutput( NL );
									/*
									if (arrayLen(rawStackTrace) gt 0){
										systemOutput(TAB & rawStackTrace[1].codePrintPlain, true);
										systemOutput(NL);
									}
									*/
								}
								if ( !isNull( specStat.error.StackTrace ) && !isEmpty( specStat.error.StackTrace ) ){
									printStackTrace( specStat.error.StackTrace );
									systemOutput( NL );
								}

							//	systemOutput(NL & serialize(specStat.error), true);

							} // if !isNull
						}
					}
				}
			} else {
				systemOutput( "WARNING: bundle.suiteStats was null?", true );
			}
			//systemOutput(serializeJson(bundle.suiteStats));
		}
		// report out any slow test specs, because for Lucee, slow performance is a bug (tm)
		if ( !isNull( bundle.suiteStats ) ) {
			loop array=bundle.suiteStats item="local.suiteStat" {
				if ( !isNull( suiteStat.specStats ) ) {
					loop array=suiteStat.specStats item="local.specStat" {
						if ( specStat.totalDuration gt 5000 )
							systemOutput( TAB & TAB & specStat.name & " took #numberFormat( specStat.totalDuration )#ms", true );
					}
				}
			}
		}

	// exceptions
	if ( !isSimpleValue( bundle.globalException ) ) {
		systemOutput( "Global Bundle Exception
		#bundle.globalException.type#
		#bundle.globalException.message#
		#bundle.globalException.detail#
=============================================================
Begin Stack Trace
=============================================================
#bundle.globalException.stacktrace#
=============================================================
  End Stack Trace
=============================================================", true);
	}

//systemOutput("=============================================================",true);
		} // try
		finally {
			SystemOut.setOut( nullValue() );
			//SystemOut.setErr(nullValue());
		} // finally
	} // onBundleEnd = function
	/*,onSuiteStart 	= function( bundle, testResults, suite ){}
	,onSuiteEnd		= function( bundle, testResults, suite ){}
	,onSpecStart		= function( bundle, testResults, suite, spec ){}
	,onSpecEnd 		= function( bundle, testResults, suite, spec ){}*/
} // callbacks
			); // report = tb.run

	 		// get the result

	}
	finally {
		//SystemOut.setOut(out);
		//SystemOut.setErr(err);
	} // try

	testResults = {
		result: tb.getResult(),
		failedTestCases: failedTestCases,
		tb: tb
	};
	return testResults;
}

}
