component hint="logs out any orm events"  {
	this.name = "global"; // used for logging out events

	function init(){
		return this;
	}

	// Currently not implemented
	function preFlush(  entity ){
		eventLog( arguments );
	}
	function onFlush( entity ) {
		eventLog( arguments );
	}
	// Currently not implemented
	function postFlush( entity ){
		eventLog( arguments );
	}

	function preLoad( entity ){
		eventLog( arguments );
	}
	function postLoad( entity ){
		eventLog( arguments );
	}

	function preInsert( entity ){
		eventLog( arguments );
	}
	function postInsert( entity ){
		eventLog( arguments );
	}

	function preUpdate( entity ){
		eventLog( arguments );
	}
	function postUpdate( entity ){
		eventLog( arguments );
	}

	function preDelete( entity ){
		eventLog( arguments );
	}	
	function onDelete( entity ) {
		eventLog( arguments );
	}
	function postDelete( entity ) {
		eventLog( arguments );
	}

	function onEvict() {
		eventLog( arguments );
	}
	function onClear( entity ) {
		eventLog( arguments );
	}
	function onDirtyCheck( entity ) {
		eventLog( arguments );
	}
	function onAutoFlush( entity ) {
		eventLog( arguments );
	}

	private function eventLog( required struct args ){
		var eventName = CallStackGet( "array" )[2].function;
		
		writeOutput( "------- #eventName# @ #this.name# ----------" );
		writeOutput( "<br>" );
		// writeDump( var = arguments.args, expand = false );

		//if ( ! structKeyExists( application, "ormEventLog" ) )
		//    application.ormEventLog = [];
		application.ormEventLog.append( {
			"src": this.name,
			"eventName": eventName,
			"args": args
		} );
	}

}