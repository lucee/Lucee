component {

	this.name = hash( getCurrentTemplatePath() );
    
	this.javasettings={
    	LoadPaths = ["../../../artifacts/jars/lucee-mockup-osgi-1.0.0.0.jar"], 
    	loadCFMLClassPath = true, 
    	reloadOnChange = false
	}
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}