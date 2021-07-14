component {
	public function init (){
		return this;
	}

	// testbox doesn't always sort the order of tests, so we do it manually LDEV-3541
	public array function getBundles(){
		var bundles = directoryList( path="/test", recurse=true, listInfo="path", filter="*.cfc" )
			.filter( filter=testFilter, parallel=true ); // directoryList doesn't allow parallel filters
		
		var rootPathLen = ExpandPath( "/test" ).len() - 4;
		ArrayEach( bundles, function( el, idx, arr ){
			var clean  = ListChangeDelims( mid( arguments.el, rootPathLen ), ".", "/\" ); // strip off dir prefix
			arguments.arr[ arguments.idx ] = mid( clean, 1, len( clean ) - 4 ); // strip off .cfc
		});
		ArraySort( bundles, "textnocase", "asc" );
		return bundles;
	}

	public boolean function testFilter ( string path ) localmode=true {
		var isValidTestCase = function ( string path ){
			// get parent
			var testDir = getDirectoryFromPath( arguments.path );
			testDir = listCompact(left( testDir, testDir.len() - 1 ), "\/" );
			if ( left( arguments.path, 1 ) eq "/")
				testDir = "/" & testDir; // avoid issues with non windows paths
			var name = listLast( arguments.path, "\/" );
			var testPath = Mid(arguments.path, len(request.testFolder) + 1); // otherwise "image" would match extension-image on CI
			switch ( true ){
				case ( left (name, 1 ) == "_" ):
					return "test has _ prefix (#name#)";
				case ( checkTestFilter( testPath ) ):
					return "excluded by testFilter";
				case ( FindNoCase( request.testFolder, testDir ) neq 1 ):
					return "not under test dir (#request.testFolder#, #testDir#)";
				case fileExists( testDir & "/Application.cfc" ):
					return "test in directory with Application.cfc";
				default:
					break;
			};
			var meta = getTestMeta( arguments.path );
			if ( !isStruct( meta ) ){
				// TODO bad cfc tickets get ignored
				// SystemOutput( "ERROR: [" & arguments.path & "] threw " & meta, true );
				return meta;
			}

			if ( meta.skip ?: false)
				return "test suite has skip=true";

			var extends = checkExtendsTestCase( meta, arguments.path );
			if ( extends neq "org.lucee.cfml.test.LuceeTestCase" )
				return "test doesn't extend Lucee Test Case (#extends#)";
			
			return checkTestLabels( meta, arguments.path );
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
			var cfcPath = ListChangeDelims( "/test" & Mid( arguments.path, len( request.testFolder ) + 1 ), ".", "/\" );
			cfcPath = mid( cfcPath, 1, len( cfcPath ) - 4 );
			try {
				// triggers a compile, which make the initial filter slower, but it would be compiled later anyway
				// GetComponentMetaData leaks output https://luceeserver.atlassian.net/browse/LDEV-3582
				silent {
					var meta = GetComponentMetaData( cfcPath );
				}
			} catch ( e ){
				return cfcatch.message;
			}
			return meta;
		};

		var checkExtendsTestCase = function (any meta, string path){
			return meta.extends.fullname ?: "";
		};

		/* testbox mixes label and skip, which is confusing, skip false should always mean skip, so we check it manually */
		var checkTestLabels = function (any meta, string path ){
			if ( arrayLen (request.testLabels) eq 0 )
				return "";
			var labels = meta.labels ?: "";
			loop array="#request.testLabels#" item="local.f" {
				if ( FindNoCase( f, labels ) gt 0 )
					return "";
			}
			return "no matching labels";
		};

		allowed = isValidTestCase( arguments.path );
		//SystemOutput( arguments.path & " :: " & allowed, true );
		if ( allowed != "" ){
			//SystemOutput( arguments.path & " :: " & allowed, true );
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
			if ( i eq 0 ){ // dump it all out
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
			var tb = new testbox.system.TestBox( bundles=getBundles(), reporter="console" );

			SystemOutput( "Found #tb.getBundles().len()# tests to run, filter took #getTickCount()-filterTimer#ms", true );
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
					if ( !isNull( suiteStat.specStats ) ) {
						loop array=suiteStat.specStats item="local.specStat" {

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
