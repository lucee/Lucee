component {

	this.name = hash( getCurrentTemplatePath() );
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}