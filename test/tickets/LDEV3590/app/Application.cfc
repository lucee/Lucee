component {

	this.Name = url.appName;
	public void function onApplicationStart(){
		sleep(100);
		application.test="test";
	}
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}