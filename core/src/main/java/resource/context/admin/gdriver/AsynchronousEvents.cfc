component extends="Gateway" {


    fields = array(
    
        field("cfc","component","",true,"CFC Path","text")

    );


    public function getLabel() {            return "Asyn Gateway" }

    public function getDescription() {      return "Handles asynchronous events through CFCs " }

    public function getCfcPath() {          return "lucee.extension.gateway.AsynchronousEvents"; }


    public function getClass() {            return ""; }

    public function getListenerPath() {     return ""; }


    // public function getListenerCfcMode() {  return "required"; }


    /*/ validate args and throw on failure
    public function onBeforeUpdate( required cfcPath, required startupMode, required custom ) {

        
    }   //*/

}