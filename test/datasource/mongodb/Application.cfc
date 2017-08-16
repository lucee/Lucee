component {

	THIS.name=url.appName;

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



	THIS.applicationTimeout = createTimeSpan( 60, 0, 0, 0 );
	THIS.sessionManagement = true;
	THIS.sessionCluster=true;
	THIS.sessionType = "cfml";
	THIS.sessionTimeout = createTimeSpan( 0, 0, 2, 0 );
	THIS.sessionStorage = "perAppCacheMongo";
	THIS.clientManagement = true;
	THIS.clientCluster = true;
	THIS.clientTimeout = createTimeSpan( 0, 2, 0, 0 );
	THIS.clientStorage = "perAppCacheMongo";

	private struct function getCredentials() {
		// getting the credetials from the enviroment variables
		var mongoDB={};
		if(!isNull(server.system.environment.MONGODB_SERVER) && !isNull(server.system.environment.MONGODB_PORT) && !isNull(server.system.environment.MONGODB_USERNAME) && !isNull(server.system.environment.MONGODB_PASSWORD)) {
			mongoDB.server=server.system.environment.MONGODB_SERVER;
			mongoDB.port=server.system.environment.MONGODB_PORT;
			mongoDB.user=server.system.environment.MONGODB_USERNAME;
			mongoDB.pass=server.system.environment.MONGODB_PASSWORD;
		}
		// getting the credetials from the system variables
		else if(!isNull(server.system.properties.MONGODB_SERVER) && !isNull(server.system.properties.MONGODB_PORT) && !isNull(server.system.properties.MONGODB_USERNAME) && !isNull(server.system.properties.MONGODB_PASSWORD)) {
			mongoDB.server=server.system.properties.MONGODB_SERVER;
			mongoDB.port=server.system.properties.MONGODB_PORT;
			mongoDB.user=server.system.properties.MONGODB_USERNAME;
			mongoDB.pass=server.system.properties.MONGODB_PASSWORD;
		}
		return mongoDB;
	}
}