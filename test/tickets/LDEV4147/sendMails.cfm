<cfscript>
    param name="form.username" default="";  

    creds = { "smtp" : server.getTestService("smtp") };
    creds.username = form.username
    threadNames="";

    for ( i=1 ; i<=10 ; i++ ) {

        threadNames = listAppend(threadNames,"LDEV-4147_#i#");

        thread action="run" creds="#creds#" name="LDEV-4147_#i#" {
            try {
                mail to = "luceeldev4147imap@localhost"
                from = "luceeldev4147@localhost"
                subject = "sending the mail for LDEV4147" 
                server="#creds.smtp.server#"
                password="#creds.smtp.password#"
                username="#creds.username#"
                port="#creds.smtp.PORT_INSECURE#"
                useTls="true"
                usessl="false"
                async="false" {
                    writeoutput("test mail for LDEV-4147");
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