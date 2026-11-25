component {
	this.name = "ldev6988_modern_" & hash( getCurrentTemplatePath() );
	server.ldev6988_hits = ( server.ldev6988_hits ?: 0 ) + 1;

	remote string function ping() {
		return "pong";
	}
}
