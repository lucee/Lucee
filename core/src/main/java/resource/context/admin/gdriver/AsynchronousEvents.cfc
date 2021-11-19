component extends="Gateway" {

    fields = array(
    
        field("cfc","component","",true,"CFC Path","text"),
        field("Interval (ms)", "interval", "60000",true,"The interval between checks, in milliseconds", "text")

    );

    public function getLabel() {            return "Async Gateway" }

    public function getDescription() {      return "Handles Asynchronous events through CFCs" }

    public function getCfcPath() {          return "lucee.extension.gateway.AsynchronousEvents"; }

    public function getClass() {            return ""; }

    public function getListenerPath() {     return "lucee.extension.gateway.AsynchronousEventsListener"; }

    public function getListenerCfcMode() {  return "required"; }

    public function onBeforeUpdate( string cfcPath, string startupMode, struct custom  ) {
 //       if(!fileExists(ExpandPath(custom.component))) throw (message="component [#custom.component#] does not exist");
    }
}