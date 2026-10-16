component {
	// track which request variables exist before running tests, used for cleanup between tests
	variables.requestKeys = {};
	structEach(request, function(key, value) {
		requestKeys[key] = true;
	});

	public function init (){
		return this;
	}

	// testbox doesn't always sort the order of tests, so we do it manually LDEV-3541
	public array function getBundles( required string testMapping, required string testDirectory, required struct testConfig ){
		var srcBundles = directoryList( path="#expandPath(arguments.testMapping)#", recurse=true, listInfo="path", filter="*.cfc" );
		var testDirectoryLen = len( arguments.testDirectory );
		var mapping = ListChangeDelims( arguments.testMapping, "", "/\" );
		var bundles = [];
		var testFilter = new _testFilter( argumentCollection = arguments.testConfig );

		arraySort( srcBundles, "textnocase", "asc" ); // make testDebug output sorted
		ArrayEach( array=srcBundles, closure=function( el, idx, arr ){
			if ( listLast( arguments.el, "\/" ) eq "Application.cfc" ) {
				// ignore 
			} else if ( testFilter.filterTest( arguments.el, testDirectory, testMapping ) ) {
				var clean  = ListChangeDelims( mid( arguments.el, testDirectoryLen + 1  ), ".", "/\" ); // strip off dir prefix
				arrayAppend(bundles, mapping & "." & mid( clean, 1, len( clean ) - 4 ) ); // strip off .cfc
			}
		}, parallel="true" );

		if ( request.testDebugAbort ){
			throw "testDebugAbort was true, exiting";
		}
		arraySort( bundles, "textnocase", "asc" );
		if ( request.testRandomSort neq "false" ) {
			if ( !isNumeric( request.testRandomSort ) ){
				request.testRandomSort = randRange( 1, 9999 ); // this way it's reproducible
			}
			systemOutput("randomize( #request.testRandomSort# );", true);
			randomize( request.testRandomSort, "SHA1PRNG" );
			loop array=#bundles# index="local.idx" item="local.a" {
				arraySwap(  bundles, local.idx, randRange( 1, arrayLen( bundles ), "SHA1PRNG" ) );
			}
		}
		return bundles;
	}
	
	public struct function runTests() localmode=true {
		SystemOut = createObject( "java", "lucee.commons.lang.SystemOut" );
		out = SystemOut.setOut( nullValue() );
		//err=SystemOut.setErr(nullValue());
		TAB = chr( 9 );
		NL = chr( 10 ) & chr( 13 );
		failedTestCases = [];

		try {
			var filterTimer = getTickCount();
			var testConfig = {
				testFilter: request.testFilter,
				testLabels: request.testLabels,
				testSkip: request.testSkip,
				testDebug: request.testDebug,
				testSuiteExtends: request.testSuiteExtends
			};

			if ( request.testExcludeDefault ) {
				systemOutput( "Excluding default tests: testExcludeDefault was true", true );
				bundles = [];
			} else {
				bundles = getBundles( "/test", request.testFolder, testConfig );
			}
			//SystemOutput( bundles, true);
			var additionalBundles = [];
			if ( len( request.testAdditional ) ){
				additionalBundles = getBundles( "/testAdditional", request.testAdditional, testConfig );
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
				for ( var b in tb.getBundles() )
					SystemOutput( b, true );
			}

			if ( len( bundles ) eq 0 ){
				SystemOutput( "Error no tests found to run, aborting", true, true );
				throw "Error no tests found to run, aborting";
			}

			// formatting is odd, because we are outputting to the console and whitespace matters
			// execute
			tb.run(callbacks=
{
	 onBundleStart = function( cfc, testResults ){
		var meta = getComponentMetadata( cfc );
		systemOutput( "" , true );
		systemOutput(structKeyList(getApplicationSettings().mappings), true );
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
			if ( bundle.totalPass eq 0 && ( bundle.totalFail + bundle.totalError ) eq 0 ){
				systemOutput( TAB & " (skipped)", true );
			} else {
				var didntPassSummary = (bundle.totalSkipped gt 0) ? ", #bundle.totalSkipped# skipped" : "";
				if ( bundle.totalError > 0 ){
					didntPassSummary &= ", #bundle.totalError# ERRORED";
				}
				if ( bundle.totalFail > 0 ){
					didntPassSummary &= ", #bundle.totalFail# FAILED";
				}
				systemOutput( TAB & " (#bundle.totalPass# tests passed in #NumberFormat(bundle.totalDuration)# ms#didntPassSummary#)", true );
			}

			// always clean up the request scope between test suites
			for ( var rr in structKeyArray( request ) ){
				if ( !structKeyExists( variables.requestKeys, rr ) ){
					systemOutput( "deleting left over request scope variable after test [#rr#]", true);
					structDelete( request, rr );
				}
			}
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

							if ( !isNull( specStat.error ) && !isEmpty( specStat.error ) ){

								var failedTestCase = {
									 type       : "Errored"
									,bundle     : bundle.name
									,testCase   : specStat.name
									,errMessage : specStat.error.Message
									,cfmlStackTrace : []
									,stackTrace : ""
								};
								if ( structKeyExists( specStat.error, "stackTrace" ) )
									failedTestCase.stackTrace = specStat.error.stackTrace;

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
											failedTestCase.cfmlStackTrace.append( frame );
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
									if (!request.keyExists("testHideJavaStack") || !request.testHideJavaStack) {
										systemOutput( TAB & specStat.error.type, true );
										// printStackTrace( specStat.error.StackTrace );
										systemOutput( TAB & specStat.error.StackTrace, true );
										systemOutput( NL );
									}
								}

							//	systemOutput(NL & serialize(specStat.error), true);

							} else if ( !isNull( specStat.failMessage ) && len( trim( specStat.failMessage ) ) ) {

								var failedTestCase = {
									 type       : "Failed"
									,bundle     : bundle.name
									,testCase   : specStat.name
									,errMessage : specStat.failMessage
									,cfmlStackTrace : []
									,stackTrace : ""
								};
								if ( structKeyExists( specStat.error, "stackTrace" ) )
									failedTestCase.stackTrace = specStat.error.stackTrace;

								failedTestCases.append( failedTestCase );

								systemOutput( NL & specStat.name );
								systemOutput( NL & TAB & "Failed: " & specStat.failMessage, true );

								if ( !isNull( specStat.failOrigin ) && !isEmpty( specStat.failOrigin ) ){

									var rawStackTrace = specStat.failOrigin;
									var testboxPath = getDirectoryFromPath( rawStackTrace[1].template );

									//systemOutput(TAB & TAB & "at", true);
									// TODO this doesn't include the stack for the actual test?

									loop array=rawStackTrace item="local.st" index="local.i" {

										if ( !st.template.hasPrefix( testboxPath ) ){
											if ( local.i eq 1 or st.template does not contain "testbox" ){
												var frame = st.template & ":" & st.line;
												failedTestCase.cfmlStackTrace.append( frame );
												systemOutput( TAB & frame, true );
											}
										}
									}
								}
								systemOutput( NL );
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

		var failedTestCase = {
			 type       : "Errored"
			,bundle     : bundle.name
			,testCase   : "Global Bundle Exception"
			,errMessage : bundle.globalException.type & ": " & bundle.globalException.message & ( len( bundle.globalException.detail ) ? NL & bundle.globalException.detail : "" )
			,cfmlStackTrace : []
			,stackTrace : bundle.globalException.stacktrace
		};

		failedTestCases.append( failedTestCase );

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

	// strips off the stack trace to exclude testbox and back to the first .cfc call in the stack
	public static array function trimJavaStackTrace( required string st, string trimToFile="" ){
		local.tab = chr( 9 );
		local.stack = [];
		
		if ( request.testDebug ?: false ){ // dump it all out
			arrayAppend( stack, TAB & arguments.st );
			return stack;
		}
		
		local.lines = listToArray( arguments.st, chr(10) );
		
		for ( local.i = 1; local.i <= arrayLen( local.lines ); local.i++ ) {
			local.line = trim( local.lines[ local.i ] );
			if ( len( local.line ) == 0 ) continue;
			
			// Skip testbox framework lines but include everything else
			if ( find( "/testbox/", local.line ) > 0 ) {
				continue;
			}
			
			arrayAppend( stack, TAB & local.line );
			
			// Check if we should stop at a specific file or use default logic
			if ( len( arguments.trimToFile ) > 0 ) {
				// Stop after finding the specified file
				if ( find( arguments.trimToFile, local.line ) > 0 ) {
					break;
				}
			} else {
				// Default behavior: stop after test code with a few extra lines
				if ( find( "/test/", local.line ) > 0 ) {
					for ( local.j = local.i + 1; local.j <= min( local.i + 3, arrayLen( local.lines ) ); local.j++ ) {
						local.nextLine = trim( local.lines[ local.j ] );
						if ( len( local.nextLine ) > 0 && find( "/testbox/", local.nextLine ) == 0 ) {
							arrayAppend( stack, TAB & local.nextLine );
						} else {
							break;
						}
					}
					break;
				}
			}
		}
		
		// Always include "Caused by" section
		local.causedByPos = find( "Caused by:", arguments.st );
		if ( local.causedByPos > 0 ) {
			arrayAppend( stack, mid( arguments.st, local.causedByPos ) );
		}
		
		return stack;
	}

}
