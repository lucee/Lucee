component {

	this.name = hash( getCurrentTemplatePath() );
    systemOutput(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"&expandPath("../../../artifacts/jars/"),1,1);
	this.javasettings={
    	bundles = [expandPath("../../../artifacts/jars/")], 
    	loadCFMLClassPath = true, 
    	reloadOnChange = false
	}
}