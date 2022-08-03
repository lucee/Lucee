<cfscript>
request._start = getTickCount();
if (execute) {

request.basedir = basedir;
request.srcall = srcall;
request.testFolder = test;

request.WEBADMINPASSWORD = "webweb";
request.SERVERADMINPASSWORD = "webweb";
server.WEBADMINPASSWORD = request.WEBADMINPASSWORD;
server.SERVERADMINPASSWORD = request.SERVERADMINPASSWORD;

NL = "
";
TAB = "	";

// this isn't used ???
fixCase = {};
for (el in ["bundleId", "debugBuffer", "endTime", "error", "failMessage", "failOrigin", "globalException", "name", "parentId", "path", "specStats",
	"startTime", "status", "suiteId", "suiteStats", "totalDuration", "totalError", "totalFail", "totalPass", "totalSkipped", "totalSpecs", "totalSuites"]) {
	fixCase[ucase(el)] = el;
}

systemOutput("Running tests with Java: #server.java.version#", true);

try {

	// create "/test" mapping
	admin
		action="updateMapping"
		type="web"
		password="#request.WEBADMINPASSWORD#"
		virtual="/test"
		physical="#request.testFolder#"
		toplevel="true"
		archive=""
		primary="physical"
		trusted="no";
	
	systemOutput("set /test mapping #dateTimeFormat(now())#", true);

	param name="testDebug" default="false";
	if ( len( testDebug ) eq 0 )
		testDebug = false;	
	request.testDebug = testDebug;
	if ( request.testDebug )
		SystemOutput( "Test Debugging enabled", true );

	param name="testServices" default="";
	request.testServices = testServices;
	if ( len( request.testServices ) )
		SystemOutput( "Test Services restricted to [#request.testServices#]", true );

	// you can also provide a json file with your environment variables, i.e. just set LUCEE_BUILD_ENV="c:\work\lucee\loader\env.json"
	setupTestServices = new test._setupTestServices().setup();

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

	systemOutput( "set admin password #dateTimeFormat(now())#", true );

	systemOutput("-------------- Test Filters and Labels", true);

	param name="testFilter" default="";	
	request.testFilter = testFilter;

	if ( len( request.testFilter ) eq 0 ){
		request.testFilter = server._getSystemPropOrEnvVars("testFilter", "", false);
		if ( structCount( request.testFilter ) )
			request.testFilter = request.testFilter.testFilter;
		else
			request.testFilter="";
	}
	request.testFilter = ListToArray( trim( request.testFilter ) );
	if ( Arraylen( request.testFilter ) gt 0 )
		systemOutput( NL & "Filtering only tests with filenames containing: " & request.testFilter.toJson() & NL, true );
	else
		systemOutput( NL & 'Running all tests, to run a subset of test(s) by FILENAME, use the parameter -DtestFilter="image,orm,etc"', true );

	param name="testLabels" default="";
	request.testLabels = testLabels;
	if ( len( trim( request.testLabels ) ) eq 0){
		request.testLabels = server._getSystemPropOrEnvVars( "testLabels", "", false);
		if ( structCount( request.testLabels ) )
			request.testLabels = request.testLabels.testLabels;
		else
			request.testLabels="";
	}
	request.testLabels = ListToArray( trim( request.testLabels ) );
	if ( ArrayLen( request.testLabels ) )
		SystemOutput( "Filtering tests with the following label(s): #request.testLabels.toJson()#", true );
	else
		systemOutput( NL & 'Running all tests, to run a subset of test(s) by LABEL, use the parameter -DtestLabels="s3,oracle"', true );

	
	param name="testSkip" default="true";
	if ( len(testSkip) eq 0)
		testSkip = true;
	request.testSkip = testSkip;

	if ( !request.testSkip )
		SystemOutput( "Force running tests marked skip=true or prefixed with an _", true );
	
	param name="testAdditional" default="";	
	request.testAdditional = testAdditional;

	if ( len( request.testAdditional ) eq 0 ){
		request.testAdditional = server._getSystemPropOrEnvVars("testAdditional", "", false);
		if ( structCount( request.testAdditional ) )
			request.testAdditional = request.testAdditional.testAdditional;
		else
			request.testAdditional="";
	}		
	if ( len(request.testAdditional) ){
		SystemOutput( "Adding additional tests from [#request.testAdditional#]", true );
		if (!DirectoryExists( request.testAdditional )){
			SystemOutput( "ERROR directory [#request.testAdditional#] doesn't exist!", true );
			request.testAdditional = "";
		} else {
			admin
				action="updateMapping"
				type="web"
				password="#request.WEBADMINPASSWORD#"
				virtual="/testAdditional"
				physical="#request.testAdditional#"
				toplevel="true"
				archive=""
				primary="physical"
				trusted="no";
		}
	}

	// output deploy log
	pc = getPageContext();
	config = pc.getConfig();
	configDir = config.getConfigServerDir();
	logsDir = configDir & server.separator.file & "logs";
	deployLog = logsDir & server.separator.file & "deploy.log";
	//dump(deployLog);
	content = fileRead( deployLog );
	
	systemOutput("-------------- Deploy.Log ------------",true);
	systemOutput( content, true );
	systemOutput("--------------------------------------",true);

	// set the testbox mapping
	application
		action="update"
		componentpaths = "#[{archive:testboxArchive}]#";

	systemOutput( "update componentpaths #dateTimeFormat( now() )#" & NL, true );

	// load testbox
	SystemOut = createObject( "java", "lucee.commons.lang.SystemOut" );
	out = SystemOut.setOut( nullValue() );
	//err = SystemOut.setErr( nullValue() );

	request._tick = getTickCount();
	request.overhead = [];

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

	systemOutput(NL & "-------------- Start Tests -----------", true);
	silent {
		testResults = new test._testRunner().runTests();
	}
	result = testResults.result;
	failedTestcases = testResults.failedTestcases;
	tb = testResults.tb;

	jUnitReporter = new testbox.system.reports.JUnitReporter();	
	resultPath = ExpandPath( "/test") & "/reports/";
	if ( !DirectoryExists( resultPath ) )
		DirectoryCreate( resultPath );
	JUnitReportFile = resultPath & "junit-test-results.xml";
	FileWrite( JUnitReportFile, jUnitReporter.runReport(results=result, testbox=tb, justReturn=true) );	
	
	systemOutput( NL & NL & "=============================================================", true );
	systemOutput( "TestBox Version: #tb.getVersion()#", true );
	systemOutput( "Lucee Version: #server.lucee.version#", true );
	systemOutput( "Java Version: #server.java.version#", true );
	systemOutput( "Total Execution time: (#NumberFormat( ( getTickCount()-request._start) / 1000 )# s)", true );
	systemOutput( "Test Execution time: (#NumberFormat( result.getTotalDuration() /1000 )# s)", true );
	systemOutput( "Average Test Overhead: (#NumberFormat( ArrayAvg( request.overhead ) )# ms)", true );
	systemOutput( "Total Test Overhead: (#NumberFormat( ArraySum( request.overhead ) )# ms)", true );

	systemOutput( "=============================================================" & NL, true );
	systemOutput( "-> Bundles/Suites/Specs: #result.getTotalBundles()#/#result.getTotalSuites()#/#result.getTotalSpecs()#", true );
	systemOutput( "-> Pass:     #result.getTotalPass()#", true );
	systemOutput( "-> Skipped:  #result.getTotalSkipped()#", true );
	systemOutput( "-> Failures: #result.getTotalFail()#", true );
	systemOutput( "-> Errors:   #result.getTotalError()#", true );
	SystemOutput( "-> JUnitReport: #JUnitReportFile#", true);

	servicesReport = new test._setupTestServices().reportServiceSkipped();
	for ( s in servicesReport ){
		systemOutput ( s, true );
	}

	if ( !isEmpty( failedTestCases ) ){
		systemOutput( NL );
		for ( el in failedTestCases ){
			systemOutput( el.type & ": " & el.bundle & NL & TAB & el.testCase, true );
			systemOutput( TAB & el.errMessage, true );
			if ( !isEmpty( el.stackTrace ) ){
				//systemOutput( TAB & TAB & "at", true);
				for ( frame in el.stackTrace ){
					systemOutput( TAB & TAB & frame, true );
				}
			}
			systemOutput( NL );
		}
		systemOutput( NL );
	}

	if ( ( result.getTotalFail() + result.getTotalError() ) > 0 ) {
		throw "TestBox could not successfully execute all testcases: #result.getTotalFail()# tests failed; #result.getTotalError()# tests errored.";
	}

	if ( ( result.getTotalError() + result.getTotalFail() + result.getTotalPass() ) eq 0 ){
		systemOutput( "", true );
		systemOutput( "ERROR: No tests were run", true );
		systemOutput( "", true );
		throw "ERROR: No tests were run";
	}

} catch( e ){
	systemOutput( "-------------------------------------------------------", true );
	systemOutput( "Testcase failed:", true );
	systemOutput( e.message, true );
	systemOutput( ReReplace( Serialize( e.stacktrace ), "[\r\n]\s*([\r\n]|\Z)", Chr( 10 ) , "ALL" ), true ); // avoid too much whitespace from dump
	systemOutput( "-------------------------------------------------------", true );
	rethrow;
}

} // if (execute)
</cfscript>
