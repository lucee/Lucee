component {

	this.name = hash( getCurrentTemplatePath() );
    
	this.javasettings={
    	LoadPaths = ["../../../artifacts/jars/lucee-mockup-classic-1.0.0.0.jar"], 
    	loadCFMLClassPath = true, 
    	reloadOnChange = false
	}
}