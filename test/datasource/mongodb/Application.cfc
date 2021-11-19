component {

	this.name=url.appName;

	variables.mongoDB=getCredentials();

	this.cache.connections["perAppCacheMongo"] = {
		class: 'org.lucee.mongodb.cache.MongoDBCache'
		, bundleName: 'mongodb.extension'
		//, bundleVersion: '3.2.2.54'
		, storage: true
		, custom:
			{
			"collection":"testsession",
			"password":mongoDB.pass,
			"connectionsPerHost":"10",
			"database":"test",
			"hosts":mongoDB.server&":"&mongoDB.port,
			"persist":"true",
			"username":mongoDB.user
			}
		, default: ''
	};

	this.applicationTimeout = createTimeSpan( 60, 0, 0, 0 );
	this.sessionManagement = true;
	this.sessionCluster=true;
	this.sessionType = "cfml";
	this.sessionTimeout = createTimeSpan( 0, 0, 2, 0 );
	this.sessionStorage = "perAppCacheMongo";
	this.clientManagement = true;
	this.clientCluster = true;
	this.clientTimeout = createTimeSpan( 0, 2, 0, 0 );
	this.clientStorage = "perAppCacheMongo";

	private struct function getCredentials() {
		// getting the credentials from the environment variables
		var mongoDB = server.getDatasource("mongoDB");
		if ( structCount(mongoDB) ){
			mongoDB.user=mongoDB.username;
			mongoDB.pass=mongoDB.password;
		}
		return mongoDB;
	}
}