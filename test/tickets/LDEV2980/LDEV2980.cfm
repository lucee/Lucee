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
		// getting the credentials from the environment variables		
		return server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "MSSQL_");;
	}
</cfscript>
