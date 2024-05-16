component output="no" {
	this.logfile    = "AsyncGateWay";

	public void function onIncomingMessage(struct CFEVENT={}){
		log text="CFMLAysncListener onIncomingMessage -Event Listener" file=this.logfile;
	}
	
	private void function _handleError(required any catchData, string functionName='unknown'){
		log text="CFMLAysncListener_handleError- Event Listener" file=this.logfile;
		  //writeDump(var="#catchData#",output="console");
	}
}