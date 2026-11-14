component {
	this.name = "LDEV3811-" & hash( getCurrentTemplatePath() );

	if ( structKeyExists( url, "appDebug" ) ) {
		this.monitoring.debuggingTemplate = true;
	}
}
