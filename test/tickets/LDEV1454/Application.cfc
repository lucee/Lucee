component{
	function onRequestStart(){
		writeOutput( serializeJSON( form ) );
		abort;
	}	
}
