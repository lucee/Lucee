component accessors=true{

    property name="listener" type="component";
    property name="config" type="struct";

    this.logfile    = "AsynGateWay";

    variables.state = "stopped";


    public function init( string id, Struct component, Struct config, Component listener) {

        variables.id       = arguments.id;
        setListener(arguments.listener);
        setConfig(arguments.config);

        writeDump(var="#arguments#", output="C:\Users\HP\Desktop\test.html", format ="classic");
     
        log text="Asyn CFC initialized path #arguments.config.component#" file=this.logfile;
    }


    public function start() {

        while ( variables.state == "stopping" )
            sleep( 10 );

        variables.state = "running";

        log text="Event Gateway #variables.id# started" file=this.logfile;

        while ( variables.state == "running" ) {

            try {


            }
            catch ( e ) {

                log text="Event Gateway #variables.id# error: #e.message#" file=this.logfile type="error";
            }

            // sleep( variables.interval );
            
        
        }

        variables.state = "stopped";
        log text="Event Gateway #variables.id# stopped" file=this.logfile;
    }


    public function stop() {

        log text="Event Gateway #variables.id# stopping" file=this.logfile;
        variables.state = "stopping";
    }


    public function restart() {

        if ( variables.state == "running" )
            this.stop();

        this.start();
    }


    public function getState() {

        return variables.state;
    }


    public function sendMessage( struct data={} ) {
        log text="sendMessage - Event Listener" file=this.logfile;
        try {
            // thread {
            local.event ={
                 cfcpath = "",
                 method="",
                 timeout="",
                 OriginatorID = "#createUUID()#",
                 CfcMethod = "",
                 Data = arguments.data,
                GatewayType="",
                hostName="#getHostName()#"
             };
            log text="sendMessage - Event Listener HostName: [#local.event.hostName#] OriginatorID: [#local.event.OriginatorId#] Available objects [#structKeyList(arguments.data)#] listenerObject:[#isobject(variables.listener)#]" file=this.logfile;
             getListener().onIncomingMessage(local.event);
            
            // }
        } catch ( any e ){
             log text="Event Gateway #e.message#" file=this.logfile;
             return false;
        }
        return true;
        // return "sendGatewayMessage() has not been implemented for the event gateway [TaskGateway]. If you want to modify it, please edit the following CFC:"& expandpath("./") & "TaskGateway.cfc";
    }

     private function getHostName() {
        try{ 
            return createObject("java", "java.net.InetAddress").getLocalHost().getHostName(); 
        }
        catch(any e) {
            _handlerError(e,'getHostName');
        }
         var sys = createObject("java", "java.lang.System");
         var hostname = sys.getenv('HOSTNAME');
        if(!isNull(hostname)) return hostname;
         var hostname = sys.getenv('COMPUTERNAME');
        if(!isNull(hostname)) return hostname;
         return 'unknown';
     }

     private void function _handlerError(required any catchData, string functionName="unknown"){
       // systemOutput('handleError',true);
       log text="#catchData.message#" file=#this.logfile#;
       // writeDump(var="#catchData#",output="console");
     }  
}