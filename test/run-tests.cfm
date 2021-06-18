<cfscript>
request._start = getTickCount();
if (execute) {

request.basedir = basedir;
request.srcall = srcall;
request.testFilter = ListToArray( trim( testFilter ) );
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

	systemOutput( "set admin password #dateTimeFormat(now())#", true );

	if ( Arraylen(request.testFilter) gt 0 )
		systemOutput( NL & "Filtering only tests containing: " & request.testFilter.toJson() & NL, true );
	else
		systemOutput( NL & 'Running all tests, to run a subset of test(s), use the parameter -DtestFilter="image,orm,etc"'& NL, true );

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