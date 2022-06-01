component{
	public function onRequestStart() {
		setting requesttimeout=10;
		writeOutput( serializeJSON( form ) );
		abort;
	}	
}
