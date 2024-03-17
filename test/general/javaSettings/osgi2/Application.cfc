component {

	this.name = hash( getCurrentTemplatePath() );
    this.javasettings={
    	bundles = [expandPath("../../../artifacts/jars/")], 
    	loadCFMLClassPath = true, 
    	reloadOnChange = false
	}
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}