component {

	this.name = hash( getCurrentTemplatePath() );
	this.datasource = server.getDatasource( "h2", server._getTempDir( "LDEV5026" ) );
	this.query.includeSQL = false;

}
