component {
	this.name = "testingApp1-"& hash( getCurrentTemplatePath() );
	this.setclientcookies = true;
	this.clientmanagement = false;
	this.sessionmanagement = true;


	public function onRequestStart() {
		setting requesttimeout=10;
	}
}