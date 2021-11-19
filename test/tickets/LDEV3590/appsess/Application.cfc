component {

	this.Name = url.appName;
	public void function onApplicationStart(){
		sleep(100);
		application.test="test";
	}
	public void function onSessionStart(){
		sleep(100);
		session.test="test";
	}
}