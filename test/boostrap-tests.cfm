<cfscript>
    encodeForHTML("abc"); // test if ESAPI extension exist right away
	systemOutput("---------- #DateTimeFormat(now(),'yyyy-mm-dd HH:nn:ss')# - Lucee Started ----------", true);

	// doing the bare minimum here, all the action happends in /test/run-tests.cfm
    // this duplicates the boostrap code in run-testcases.xml

    param name="test" default="";
    if (len(test) eq 0)
        test = GetDirectoryFromPath(GetCurrentTemplatePath());

	request.WEBADMINPASSWORD = "webweb";
	request.SERVERADMINPASSWORD = "webweb";
	server.WEBADMINPASSWORD = request.WEBADMINPASSWORD;
	server.SERVERADMINPASSWORD = request.SERVERADMINPASSWORD;

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

	 include template="run-tests.cfm";
</cfscript>