<cfscript>
	//encodeForHTML("abc"); // test if ESAPI extension exist right away
	systemOutput("---------- #DateTimeFormat(now(),'yyyy-mm-dd HH:nn:ss')# - Lucee Started ----------", true);
	systemOutput("Lucee version: #server.lucee.version# (Java #server.java.version#)", true);

	setting requesttimeout = 10*60; // 10 mins, for when running via script-runner and _internalRequest

	// doing the bare minimum here, all the action happends in /test/run-tests.cfm
	// this duplicates the boostrap code in run-testcases.xml

	param name="test" default="";
	param name="testFilter" default="";
	param name="baseDir" default="";
	param name="srcAll" default="../core/src/main/"; // used for compiling

	param name="testBoxArchive" default="";

	if (len(test) eq 0){
		test = GetDirectoryFromPath(GetCurrentTemplatePath());
		test =left(test, len(test)-1);
		systemOutput(test, true);
	}

	if ( len( baseDir ) eq 0 )
		basedir=test; // allow running test suite via browser

	request.SERVERADMINPASSWORD = "webweb";
	server.SERVERADMINPASSWORD = request.SERVERADMINPASSWORD;

	oldpassword="admin";

	systemOutput( "set web admin password", true);

	systemOutput( "set server admin password", true );
	try {
		admin
			action="updatePassword"
			type="server"
			oldPassword="#oldpassword#"
			newPassword="#request.SERVERADMINPASSWORD#";
	}
	catch(e){
		systemOutput( cfcatch.message, true);
	}	// may exist from previous execution

	// create "/test" mapping
	admin
		action="updateMapping"
		type="server"
		password="#request.SERVERADMINPASSWORD#"
		virtual="/test"
		physical="#test#"
		toplevel="true"
		archive=""
		primary="physical"
		trusted="no";

	// reset the webroot to be empty, to avoid any conflicting mappings
	empty_webroot = "#getTempDirectory()#\empty_webroot";
	if ( !directoryExists( empty_webroot ) )
		directoryCreate( empty_webroot );
	admin
		action="updateMapping"
		type="server"
		password="#request.SERVERADMINPASSWORD#"
		virtual="/"
		physical="#empty_webroot#"
		toplevel="true"
		archive=""
		primary="physical"
		trusted="no";

	if ( len(testBoxArchive) eq 0 ){
		testboxVersion = "3.2.0";
		testboxArchive = "#getTempDirectory()#\testbox.zip";
		testboxUrl ="https://downloads.ortussolutions.com/ortussolutions/testbox/#testboxVersion#/testbox-#testboxVersion#.zip";

		systemOutput( "Downloading [#testboxUrl#]", true );

		fileWrite( testboxArchive, FileReadBinary( testboxUrl ) );
		testboxDir = "#getTempDirectory()#testbox";

		extract( format="zip", source=testboxArchive, target=getTempDirectory() );
		testboxArchive = testboxDir;

		admin
			action="updateMapping"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			virtual="/testbox"
			physical="#testboxdir#"
			toplevel="true"
			archive=""
			primary="physical"
			trusted="no";
	}
	execute = true;

	include template="/test/run-tests.cfm";
</cfscript>