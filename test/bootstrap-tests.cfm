<cfscript>
	encodeForHTML("abc"); // test if ESAPI extension exist right away
	systemOutput("---------- #DateTimeFormat(now(),'yyyy-mm-dd HH:nn:ss')# - Lucee Started ----------", true);

	// doing the bare minimum here, all the action happends in /test/run-tests.cfm
	// this duplicates the boostrap code in run-testcases.xml

	param name="test" default="";
	param name="testFilter" default="";
	param name="srcAll" default="../core/src/main/"; // used for compiling

	param name="testBoxArchive" default=""; 
	
	if (len(test) eq 0){
		test = GetDirectoryFromPath(GetCurrentTemplatePath());
		test =left(test, len(test)-1);
		systemOutput(test, true);
	}


	request.WEBADMINPASSWORD = "webweb";
	request.SERVERADMINPASSWORD = "webweb";
	server.WEBADMINPASSWORD = request.WEBADMINPASSWORD;
	server.SERVERADMINPASSWORD = request.SERVERADMINPASSWORD;

	oldpassword="admin";

	systemOutput( "set web admin password", true);

	// set a password for the admin
	try {
		admin
			action="updatePassword"
			type="web"
			oldPassword="#oldpassword#"
			newPassword="#request.WEBADMINPASSWORD#";
	}
	catch(e){
		systemOutput( cfcatch.message, true);
	}	// may exist from previous execution

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
		type="web"
		password="#request.WEBADMINPASSWORD#"
		virtual="/test"
		physical="#test#"
		toplevel="true"
		archive=""
		primary="physical"
		trusted="no";

	// reset the webroot to be empty, to avoid any conflicting mappings
	empty_webroot = "#getTempDirectory()#\empty_webroot";
	DirectoryCreate(empty_webroot);
	admin
		action="updateMapping"
		type="web"
		password="#request.WEBADMINPASSWORD#"
		virtual="/"
		physical="#empty_webroot#"
		toplevel="true"
		archive=""
		primary="physical"
		trusted="no";

	if ( len(testBoxArchive) eq 0 ){
		testboxVersion = "2.2.0";
		testboxArchive = "#getTempDirectory()#\testbox.zip";
		testboxUrl ="https://downloads.ortussolutions.com/ortussolutions/testbox/#testboxVersion#/testbox-#testboxVersion#.zip";

		systemOutput( "Downloading [#testboxUrl#]", true );

		fileWrite( testboxArchive, FileReadBinary( testboxUrl ) );
		testboxDir = "#getTempDirectory()#testbox";

		// we need the compress extension, if running with lucee light, it won't be installed yet
		if ( !extensionExists("8D7FB0DF-08BB-1589-FE3975678F07DB17") ){
			throw "Lucee Compress extension is required, but not installed, are you using the Lucee light jar?";
		}		

		zip action="unzip" file="#testboxArchive#" destination="#getTempDirectory()#";
		testboxArchive = testboxDir;

		admin
			action="updateMapping"
			type="web"
			password="#request.WEBADMINPASSWORD#"
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