component {
	this.name = url.name;
    this.sessionManagement = true;
    this.sessionTimeout = createTimeSpan( 0, 1, 0, 0);

    public boolean function onApplicationStart() {
 		echo('-onApplicationStart-');
 		return true;
 	}
    public boolean function onSessionStart() {
 		echo('-onSessionStart-');
 		return true;
 	}
	
	 public function onRequestStart() {
		 setting requesttimeout=10;
	 }
 
}