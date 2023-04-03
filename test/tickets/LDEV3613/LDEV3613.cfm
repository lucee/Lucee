<cfscript>
    param name="FORM.sleep" default="false";
    
    application.test = "success";

    try {
        sleep(50);
        if (form.sleep) sleep(2000);
        writeoutput(application.test);
        applicationStop();
    }
    catch(any e){
        writeoutput(e.message);
    }
</cfscript>