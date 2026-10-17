component {
	this.name = "LDEV6070_disabled_" & hash( getCurrentTemplatePath() );

	// Allow controlling formUrlAsStruct via URL parameter
	this.formUrlAsStruct = url.formUrlAsStruct ?: false;
}
