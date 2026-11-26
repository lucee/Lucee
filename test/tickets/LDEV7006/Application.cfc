component {
	this.name = "LDEV-7006-" & ( url.appParser ?: "none" );
	if ( len( url.appParser ?: "" ) ) this.dateParser = url.appParser;
}
