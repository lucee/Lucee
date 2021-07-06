component {
    this.name = "test-LDEV-3590";
    function onApplicationStart() {
        //systemOutput("application started [#cgi.QUERY_STRING#] #now()#", true);
        sleep(500); // force race condition for second request, application.applicationStarted won't exist
        application.applicationStarted = true;
        //systemOutput("application started, var set [#cgi.QUERY_STRING#] #now()#", true);
    }
    function onRequestStart(){
        //systemOutput("on request start [#cgi.QUERY_STRING#] #now()#", true);
    }
}