component {

	postgreSQL = getCredentials();

	this.name = "luceetestpostgreSQL";
	this.datasources["ldev2566_POSTGRESQL"] = {
		class : 'org.postgresql.Driver'
		, bundleName : 'org.postgresql.jdbc'
		, connectionString : 'jdbc:postgresql://'&postgreSQL.server&':'&postgreSQL.PORT&'/'&postgreSQL.DATABASE
		, username : postgreSQL.username
		, password : postgreSQL.password
	};
	this.datasource = "ldev2566_POSTGRESQL";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2566_postTable");
		}
		query{
			echo("CREATE TABLE LDEV2566_postTable( id serial, name varchar(20))");
		}
	}
	private struct function getCredentials() {
		// getting the credetials from the enviroment variables
		var postgreSQL = {};
		if(
			!isNull(server.system.environment.POSTGRESQL_SERVER) && 
			!isNull(server.system.environment.POSTGRESQL_USERNAME) && 
			!isNull(server.system.environment.POSTGRESQL_PASSWORD) && 
			!isNull(server.system.environment.POSTGRESQL_PORT) && 
			!isNull(server.system.environment.POSTGRESQL_DATABASE)) {
			postgreSQL.server = server.system.environment.POSTGRESQL_SERVER;
			postgreSQL.username = server.system.environment.POSTGRESQL_USERNAME;
			postgreSQL.password = server.system.environment.POSTGRESQL_PASSWORD;
			postgreSQL.port = server.system.environment.POSTGRESQL_PORT;
			postgreSQL.database = server.system.environment.POSTGRESQL_DATABASE;
		}
		// getting the credetials from the system variables
		else if(
			!isNull(server.system.properties.POSTGRESQL_SERVER) && 
			!isNull(server.system.properties.POSTGRESQL_USERNAME) && 
			!isNull(server.system.properties.POSTGRESQL_PASSWORD) && 
			!isNull(server.system.properties.POSTGRESQL_PORT) && 
			!isNull(server.system.properties.POSTGRESQL_DATABASE)) {
			postgreSQL.server = server.system.properties.POSTGRESQL_SERVER;
			postgreSQL.username = server.system.properties.POSTGRESQL_USERNAME;
			postgreSQL.password = server.system.properties.POSTGRESQL_PASSWORD;
			postgreSQL.port = server.system.properties.POSTGRESQL_PORT;
			postgreSQL.database = server.system.properties.POSTGRESQL_DATABASE;
		}
		return postgreSQL;
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2566_postTable");
		}
	}
}