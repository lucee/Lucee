component  {

	function init(){
		return this;
	}

	function preFlush( entity ){
		eventLog( arguments );
	}

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

	function preUpdate(entity, Struct oldData){
		eventLog( arguments );
	}

	function postUpdate( entity ){
		eventLog( arguments );
	}

	function preDelete( entity ){
		eventLog( arguments );
	}
	
	function postDelete( entity ) {
		eventLog( arguments );
	}

	private function eventLog(required struct args){
		var eventName = CallStackGet( "array" )[2].function;
		
		systemOutput( "------- #eventName# ----------", true );
		systemOutput( arguments.args, true );

		//if ( ! structKeyExists( application, "ormEventLog" ) )
		//    application.ormEventLog = [];
		application.ormEventLog.append( {
			"eventName": eventName,
			"args": args
		} );
	}

}