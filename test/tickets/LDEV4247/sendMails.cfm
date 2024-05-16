<cfscript>
    param name="form.username" default="";
    creds = { "smtp" : server.getTestService("smtp") };
    creds.username = form.username
    threadNames="";

    for ( i=1 ; i<=5 ; i++ ) {

        threadNames = listAppend(threadNames,"LDEV-4247_#i#");

        thread action="run" creds="#creds#" name="LDEV-4247_#i#" {
            try {
                mail to = "#creds.username#"
                from = "luceeldev4247@localhost"
                subject = "sending the mail for LDEV4247" 
                server="#creds.smtp.server#"
                password="#creds.smtp.password#"
                username="luceeldev4247@localhost"
                port="#creds.smtp.PORT_INSECURE#"
                useTls="true"
                usessl="false"
                async="false" {
                    writeoutput("test mail for LDEV-4247");
                }
            } catch (any e) {
                server.mailsErrorMessage = e.message;
            }
        }
    }

    thread action="join" name="#threadNames#";

    if (structKeyExists(server, "mailsErrorMessage")) {
        writeoutput("sending mails failed with: #server.mailsErrorMessage#");
    }
    else writeoutput("Done!!!");
</cfscript>