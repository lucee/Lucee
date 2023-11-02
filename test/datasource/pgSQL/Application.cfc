component {

	pgSQL = getCredencials();

	this.name = "luceetest";
	this.datasources["pgSQL_DSN"] = {
		  class: 'org.postgresql.Driver'
		, bundleName: 'org.postgresql.jdbc42'
		, bundleVersion: '9.4.1212'
		, connectionString: 'jdbc:postgresql://localhost:5432/test'
		, username: #pgSQL.username#
		, password: #pgSQL.password#
	};
	this.datasource = "pgSQL_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS test_pgSQL");
		}
		query{
			echo("CREATE TABLE test_pgSQL( id int, name varchar(20), age int)");
		}
	}
	
	private struct function getCredencials() {
		var pgSQL={};
		if(
			!isNull(server.system.environment.POSTGRES_SERVER) && 
			!isNull(server.system.environment.POSTGRES_USERNAME) && 
			!isNull(server.system.environment.POSTGRES_PASSWORD) && 
			!isNull(server.system.environment.POSTGRES_PORT) && 
			!isNull(server.system.environment.POSTGRES_DATABASE)) {
			pgSQL.server=server.system.environment.POSTGRES_SERVER;
			pgSQL.username=server.system.environment.POSTGRES_USERNAME;
			pgSQL.password=server.system.environment.POSTGRES_PASSWORD;
			pgSQL.port=server.system.environment.POSTGRES_PORT;
			pgSQL.database=server.system.environment.POSTGRES_DATABASE;
		}

		else if(
			!isNull(server.system.properties.POSTGRES_SERVER) && 
			!isNull(server.system.properties.POSTGRES_USERNAME) && 
			!isNull(server.system.properties.POSTGRES_PASSWORD) && 
			!isNull(server.system.properties.POSTGRES_PORT) && 
			!isNull(server.system.properties.POSTGRES_DATABASE)) {
			pgSQL.server=server.system.properties.POSTGRES_SERVER;
			pgSQL.username=server.system.properties.POSTGRES_USERNAME;
			pgSQL.password=server.system.properties.POSTGRES_UPASSWORD;
			pgSQL.port=server.system.properties.POSTGRES_PORT;
			pgSQL.database=server.system.properties.POSTGRES_DATABASE;
		}
		return pgSQL;
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS test_pgSQL");
		}
	}
}
