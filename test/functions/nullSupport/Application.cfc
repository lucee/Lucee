component {
	this.name = "null-" & createUUID();
	this.nullSupport = true;
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}