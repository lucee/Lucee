<cfscript>
	param name="form.scene" default="1";
	msSQL = getCredentials();

	if(form.scene eq 1) {

		adm = new Administrator('server', request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD);
		try{
		    adm.updateDatasource(
		    name: 'datasource1',
		    newname: 'datasource5',
		    type: 'MSSQL',
		    host: '#msSQL.SERVER#',
		    database: #msSQL.DATABASE#,
		    port: #msSQL.PORT#,
		    username: #msSQL.USERNAME#,
		    password: #msSQL.PASSWORD#,
		    connectionLimit: 100,
		    connectionTimeout: 12,
		    storage: false,
		    blob: true,
		    clob: true,
		    verify: true
		    );
		    writeOutput("success");
		}
		catch(any e){
		    writeOutput(e.message);
		}
	}
</cfscript>

<cfadmin action="getDatasource"
	type="server"
	password="#request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD#"
	name="datasource5"
	returnVariable="datasource">
<cfscript>
	
	if(form.scene eq 2) {
		
		adm = new Administrator('server', request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD);
		try{
		    adm.updateDatasource(
		    name: 'datasource1',
		    newname: 'datasource5',
		    type: 'MSSQL',
		    host: '#msSQL.SERVER#',
		    database: #msSQL.DATABASE#,
		    port: #msSQL.PORT#,
		    username: #msSQL.USERNAME#,
		    password:'#datasource.passwordEncrypted#',
		    connectionLimit: 100,
		    connectionTimeout: 12,
		    storage: false,
		    blob: true,
		    clob: true,
		    verify: true
		    );
		    writeOutput("success");
		}
		catch(any e){
		    writeOutput(e.message);
		}
	}


	private struct function getCredentials() {
		// getting the credetials from the enviroment variables
		var msSQL={};
		if(
			!isNull(server.system.environment.MsSQL_SERVER) && 
			!isNull(server.system.environment.MsSQL_USERNAME) && 
			!isNull(server.system.environment.MsSQL_PASSWORD) && 
			!isNull(server.system.environment.MsSQL_PORT) && 
			!isNull(server.system.environment.MsSQL_DATABASE)) {
			msSQL.server=server.system.environment.MsSQL_SERVER;
			msSQL.username=server.system.environment.MsSQL_USERNAME;
			msSQL.password=server.system.environment.MsSQL_PASSWORD;
			msSQL.port=server.system.environment.MsSQL_PORT;
			msSQL.database=server.system.environment.MsSQL_DATABASE;
		}
		// getting the credetials from the system variables
		else if(
			!isNull(server.system.properties.MsSQL_SERVER) && 
			!isNull(server.system.properties.MsSQL_USERNAME) && 
			!isNull(server.system.properties.MsSQL_PASSWORD) && 
			!isNull(server.system.properties.MsSQL_PORT) && 
			!isNull(server.system.properties.MsSQL_DATABASE)) {
			msSQL.server=server.system.properties.MsSQL_SERVER;
			msSQL.username=server.system.properties.MsSQL_USERNAME;
			msSQL.password=server.system.properties.MsSQL_PASSWORD;
			msSQL.port=server.system.properties.MsSQL_PORT;
			msSQL.database=server.system.properties.MsSQL_DATABASE;
		}
		return msSql;
	}
</cfscript>
