<cfscript>
    //systemOutput("test.cfm [#cgi.QUERY_STRING#] #now()#", true );
    //systemOutput("vars: [#structKeyList(application)#] [#cgi.QUERY_STRING#] #now()#", true );
    if ( structKeyExists(url, "stop") ){
        applicationStop();
        //systemOutput( "applicationStop()", true );
    }

    //systemOutput("#structKeyExists( application, "applicationStarted" )# [#cgi.QUERY_STRING#] #now()#", true );
    echo( structKeyExists( application, "applicationStarted" ) );
</cfscript>
