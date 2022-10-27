<cfscript>
    param name="FORM.uids" default="";
    param name="FORM.scene" default="";
    param name="FORM.username" default="";

    creds = {
			imap : server.getTestService("imap"),
			pop : server.getTestService("pop")
		}

    if (form.scene == 1 || form.scene == 2) {

        attrs = { 
            action="getAll",
            server="#creds.pop.SERVER#" ,
            password="#creds.pop.PASSWORD#", 
            port="#creds.pop.PORT_INSECURE#",
            secure="no", 
            username="#form.username#",
            name="result",
            delimiter="|$" 
        }
            
        if (form.uids != "") attrs.uid = form.uids;

        pop attributeCollection=attrs;

        writeoutput(result.recordCount);

    }
    else if (form.scene == 3 || form.scene == 4) {
        attrs = {
			action="getAll",
			server="#creds.imap.SERVER#",
			username="#form.username#",
			password="#creds.imap.PASSWORD#",
			port="#creds.imap.PORT_INSECURE#",
			secure="no",
			name="result",
            delimiter="|$"
		}

		if (form.uids != "") attrs.uid = form.uids;

		imap attributeCollection = "#attrs#";

        writeoutput(result.recordCount);
    }
</cfscript>