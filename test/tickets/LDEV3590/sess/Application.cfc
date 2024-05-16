component {

	this.Name = url.appName;
	public void function onSessionStart(){
		sleep(100);
		session.test="test";
	}
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}