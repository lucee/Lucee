component {
	this.name = "testingApp2"& hash( getCurrentTemplatePath() );
	this.setclientcookies = true;
	this.clientmanagement = true;
	this.sessionmanagement = true;

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}