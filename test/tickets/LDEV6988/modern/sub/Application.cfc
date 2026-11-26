component {
	this.name = "ldev6988_modern_sub_" & hash( getCurrentTemplatePath() );
	server.ldev6988_hits = ( server.ldev6988_hits ?: 0 ) + 1;
}
