component hint="logs out any orm events"  {
	this.name = "global"; // used for logging out events

	function init(){
		return this;
	}

	function preFlush( required any entity ){
		eventLog( arguments );
	}
	function postFlush(required any entity){
		eventLog( arguments );
	}

	function preLoad(required any entity){
		eventLog( arguments );
	}
	function postLoad(required any entity){
		eventLog( arguments );
	}

	function preInsert(required any entity){
		eventLog( arguments );
	}
	function postInsert(required any entity){
		eventLog( arguments );
	}

	function preUpdate(required any entity, Struct oldData){
		eventLog( arguments );
	}
	function postUpdate(required any entity){
		eventLog( arguments );
	}

	function preDelete(required any entity){
		eventLog( arguments );
	}	
	function postDelete(required any entity) {
		eventLog( arguments );
	}

	private function eventLog(required struct args){
		var eventName = CallStackGet( "array" )[2].function;
		
		systemOutput( "------- #eventName# @ #this.name# ----------", true );
		systemOutput( arguments.args, true );

		//if ( ! structKeyExists( application, "ormEventLog" ) )
		//    application.ormEventLog = [];
		application.ormEventLog.append( {
			"src": this.name,
			"eventName": eventName,
			"args": args
		} );
	}

}