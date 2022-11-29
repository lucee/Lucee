<cfscript>
 encodeForHTML("abc"); // test if ESAPI extension exist right away
if (execute) {

request.basedir = basedir;
request.srcall = srcall;
request.WEBADMINPASSWORD = "webweb";
request.SERVERADMINPASSWORD = "webweb";
server.WEBADMINPASSWORD = request.WEBADMINPASSWORD;
server.SERVERADMINPASSWORD = request.SERVERADMINPASSWORD;

NL = "
";
TAB = "	";

fixCase = {};
for (el in ["bundleId", "debugBuffer", "endTime", "error", "failMessage", "failOrigin", "globalException", "name", "parentId", "path", "specStats", "startTime", "status", "suiteId", "suiteStats", "totalDuration", "totalError", "totalFail", "totalPass", "totalSkipped", "totalSpecs", "totalSuites"
]) {
	fixCase[ucase(el)] = el;
}

failedTestCases = [];

try {

function mem(type) {
    var qry = getMemoryUsage(type);
    loop query=qry {
        var perc = int(100 / qry.max * qry.used);
        if(qry.max<0 || qry.used<0 || perc<90)
        	continue;
        systemOutput(TAB & replace(ucFirst(type), '_', ' ') & " " & qry.name & ": " & perc & "%", true);
    }
}

// set a password for the admin
	try {
		admin
			action="updatePassword"
			type="web"
			oldPassword=""
			newPassword="#request.WEBADMINPASSWORD#";
	}
	catch(e){}	// may exist from previous execution

	try {
		admin
			action="updatePassword"
			type="server"
			oldPassword=""
			newPassword="#request.SERVERADMINPASSWORD#";
	}
	catch(e){}	// may exist from previous execution

	systemOutput("set password #dateTimeFormat(now())#", true);

	// output deploy log
	pc=getPageContext();
	config=pc.getConfig();
	configDir=config.getConfigServerDir();
	logsDir=configDir&server.separator.file&"logs";
	deployLog=logsDir&server.separator.file&"deploy.log";
	// dump(deployLog);  // causes NPE sometimes https://luceeserver.atlassian.net/browse/LDEV-3616
	content=fileRead(deployLog);
	systemOutput("-------------- Deploy Log ------------",true);
	systemOutput(content,true);
	systemOutput("--------------------------------------",true);


	// create "/test" mapping
	admin
		action="updateMapping"
		type="web"
		password="#request.WEBADMINPASSWORD#"
		virtual="/test"
		physical="#test#"
		toplevel="true"
		archive=""
		primary="physical"
		trusted="no";

	systemOutput("set /test mapping #dateTimeFormat(now())#", true);

	// you can also provide a json file with your environment variables, i.e. just set LUCEE_BUILD_ENV="c:\work\lucee\loader\env.json"
	setupTestServices = new test._setupTestServices().setup();	

	// set the testbox mapping
	application
		action="update"
		componentpaths = "#[{archive:testboxArchive}]#";

	systemOutput("update componentpaths #dateTimeFormat(now())#", true);

	admin
		action="getMappings"
		type="web"
		password="#request.WEBADMINPASSWORD#"
		returnVariable="mappings";

	systemOutput("-------------- Mappings --------------", true);
	loop query="mappings" {
		systemOutput("#mappings.virtual# #TAB# #mappings.strPhysical# "
			& (len(mappings.strArchive) ? "[#mappings.strArchive#] " : "")
			& (len(mappings.inspect) ? "(#mappings.inspect#)" : ""), true);
	}
	systemOutput("", true);

	// load testbox
	SystemOut=createObject("java", "lucee.commons.lang.SystemOut");
	out=SystemOut.setOut(nullValue());
	//err=SystemOut.setErr(nullValue());

	testDirectory = expandPath( "/test" );
	
	_getTestMeta = function ( string path ){
		var cfcPath = ListChangeDelims( "/test" & Mid( arguments.path, len( testDirectory ) + 1 ), ".", "/\" );
		cfcPath = mid( cfcPath, 1, len( cfcPath ) - 4 ); // strip off ".cfc"
		try {
			// triggers a compile, which make the initial filter slower, but it would be compiled later anyway
			// GetComponentMetaData leaks output https://luceeserver.atlassian.net/browse/LDEV-3582
			silent {
				var meta = GetComponentMetaData( cfcPath );
			}
		} catch ( e ){
			systemOutput("ERROR calling GetComponentMetaData on #cfcPath#", true);
			systemOutput(e.message, true);
			return {
				"_exception": cfcatch
			}
		}
		return meta;
	};

	silent {
		try {

			dir = {
				 mapping : "/test"
				,recurse : true
				,filter  : function(path){
		//			echo(arguments.path&"
		//");
					var name=listLast(arguments.path,"\/");

					// get parent
					var p = getDirectoryFromPath(arguments.path);
					p = left(p, p.len() - 1);
					p = listTrim(p, "\/");

					// get grand parent
					var pp = getDirectoryFromPath(p);
					pp = left(pp, pp.len() - 1);
					pp = listTrim(pp, "\/");

					// only testcases in sub directory of "test" are allowed
					var _test = listTrim(test,"\/");
					var allowed = (_test == pp || _test == p) && left(name, 1) != "_";
					if ( allowed ){
						// check for skip=true on test case components
						var meta = _getTestMeta( path );
						if ( structKeyExists( meta, "skip" ) and meta.skip eq true )
							allowed = false;
					}
					return allowed;
				}
			};

			tb = new testbox.system.TestBox(directory=dir, reporter="console");

			// execute
			report = tb.run(callbacks=
{
	 onBundleStart = function(cfc, testResults){
		var meta = getComponentMetadata(cfc);
		SystemOut.setOut(out);
		//SystemOut.setErr(err);
		systemOutput("=============================================================" & NL & meta.name, false);
		SystemOut.setOut(nullValue());
		//SystemOut.setErr(nullValue());
	} // onBundleStart = function
	,onBundleEnd = function(cfc, testResults){
		var bundle = arrayLast(testResults.getBundleStats());
		try {
			SystemOut.setOut(out);
			//SystemOut.setErr(err);

			if ( bundle.totalPass eq 0 )
			systemOutput( TAB & " (skipped)", true );
		else
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
								systemOutput( TAB & specStat.error.type, true );
								// printStackTrace( specStat.error.StackTrace );
								systemOutput( TAB & specStat.error.StackTrace, true );
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
	if (!isSimpleValue(bundle.globalException)) {
		systemOutput("Global Bundle Exception
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
			SystemOut.setOut(nullValue());
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
	 		result = tb.getResult();
 		}
 		finally {
 			//SystemOut.setOut(out);
 			//SystemOut.setErr(err);
 		} // try
	} // silent

	jUnitReporter = new testbox.system.reports.JUnitReporter();
	resultPath = ExpandPath( "/test" ) & "/reports/";
	if ( !DirectoryExists( resultPath ) )
		DirectoryCreate( resultPath );
	JUnitReportFile = resultPath & "junit-test-results.xml";
	FileWrite( JUnitReportFile, jUnitReporter.runReport( results=result, testbox=tb, justReturn=true ) );	
	
	systemOutput("", true );
	systemOutput("", true );
	systemOutput("=============================================================", true );
	systemOutput("TestBox Version: #tb.getVersion()#", true );
	systemOutput("Lucee Version: #server.lucee.version#", true );
	systemOutput("Java Version: #server.java.version#", true );
	systemOutput("Global Stats (#result.getTotalDuration()# ms)", true );
	systemOutput("=============================================================", true );
	systemOutput("->[Bundles/Suites/Specs: #result.getTotalBundles()#/#result.getTotalSuites()#/#result.getTotalSpecs()#]",true);
	systemOutput("->[Pass:     #result.getTotalPass()#]", true );
	systemOutput("->[Skipped:  #result.getTotalSkipped()#]", true );
	systemOutput("->[Failures: #result.getTotalFail()#]", true );
	systemOutput("->[Errors:   #result.getTotalError()#]", true );
	systemOutput("->[JUnitReport: #JUnitReportFile#]", true );

	// load errors into an array, so we can dump them out to $GITHUB_STEP_SUMMARY
	results = [];
	results_md = [];
	request.testDebug = false;

	if ( structKeyExists( server.system.environment, "GITHUB_STEP_SUMMARY" ) ){
		github_commit_base_href=  "/" & server.system.environment.GITHUB_REPOSITORY
			& "/blob/" & server.system.environment.GITHUB_SHA & "/";
		github_branch_base_href=  "/" & server.system.environment.GITHUB_REPOSITORY
			& "/blob/" & server.system.environment.GITHUB_REF_NAME & "/";
	}

	if ( !isEmpty( failedTestCases ) ){
		systemOutput( NL );
		for ( el in failedTestCases ){
			arrayAppend( results, el.type & ": " & el.bundle & NL & TAB & el.testCase );
			arrayAppend( results, TAB & el.errMessage );
			arrayAppend( results_md, "#### " & el.type & " " & el.bundle );
			arrayAppend( results_md, "###### " & el.testCase );
			arrayAppend( results_md, "" );
			arrayAppend( results_md, el.errMessage );

			if ( !isEmpty( el.cfmlStackTrace ) ){
				//arrayAppend( results, TAB & TAB & "at", true);
				for ( frame in el.cfmlStackTrace ){
					arrayAppend( results, TAB & TAB & frame );
					if ( structKeyExists( server.system.environment, "GITHUB_STEP_SUMMARY" ) ){
						file_ref = replace( frame, server.system.environment.GITHUB_WORKSPACE, "" );
						arrayAppend( results_md,
							"- [#file_ref#](#github_commit_base_href##replace(file_ref,":", "##L")#)"
							& " [branch](#github_branch_base_href##replace(file_ref,":", "##L")#)" );
					}
				}
			}

			if ( !isEmpty( el.StackTrace ) ){
				arrayAppend( results_md, "" );
				arrayAppend( results, NL );
				arrStack = test._testRunner::trimJavaStackTrace( el.StackTrace );
				for (s in arrStack) {
					arrayAppend( results, s );
					arrayAppend( results_md, s );
				}
			}

			arrayAppend( results_md, "" );
			arrayAppend( results, NL );
		}
		arrayAppend( results_md, "" );
		arrayAppend( results, NL );
	}

	if ( len( results ) ) {
		loop array=#results# item="resultLine" {
			systemOutput( resultLine, (resultLine neq NL) );
		}
		if ( structKeyExists( server.system.environment, "GITHUB_STEP_SUMMARY" ) ){
			//systemOutput( server.system.environment.GITHUB_STEP_SUMMARY, true );
			fileWrite( server.system.environment.GITHUB_STEP_SUMMARY, ArrayToList( results_md, NL ) );
		}
		/*
		loop collection=server.system.environment key="p" value="v" {
			if ( p contains "GITHUB_")
				systemOutput("#p#: #v##NL#");
		*/
	} else if ( structKeyExists( server.system.environment, "GITHUB_STEP_SUMMARY" ) ){
		fileWrite( server.system.environment.GITHUB_STEP_SUMMARY, "#### Tests Passed :white_check_mark:" );
	}


 		if ((result.getTotalFail() + result.getTotalError()) > 0) {
 			throw "TestBox could not successfully execute all testcases: #result.getTotalFail()# tests failed; #result.getTotalError()# tests errored.";
 		}
	}
	catch(e){
		systemOutput("-------------------------------------------------------", true );
		systemOutput("Testcase failed:", true );
		systemOutput( e.message, true );
		systemOutput( serialize(e), true );
		systemOutput("-------------------------------------------------------", true );
		rethrow;
	}

} // if (execute)
</cfscript>