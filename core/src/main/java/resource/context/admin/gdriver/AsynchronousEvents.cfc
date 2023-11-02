component extends="Gateway" {

    fields = array(
    
        field("cfc","component","",true,"CFC Path","text")

    );

    public function getLabel() {            return "Asyn Gateway" }

    public function getDescription() {      return "Handles asynchronous events through CFCs" }

    public function getCfcPath() {          return "lucee.extension.gateway.AsynchronousEvents"; }


    public function getClass() {            return ""; }

    public function getListenerPath() {     return "lucee.extension.gateway.AsynchronousEventsListener"; }


    public function getListenerCfcMode() {  return "required"; }


    public function onBeforeUpdate( string cfcPath, string startupMode, struct custom  ) {
        if(!fileExists(custom.component)) throw (message="component [#custom.component#] does not exist");
    }
}