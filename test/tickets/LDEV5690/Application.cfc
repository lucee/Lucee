component {

	this.name = "LDEV5690-" & hash( getCurrentTemplatePath() );

	// Allow controlling limitEvaluation via URL parameter for testing
	this.security.limitEvaluation = url.limit;

}
