<cfcomponent>
	<cfscript>
		this.name = "testcase";
		orc=getCredencials();
		// otherwise we get the following on travis ORA-00604: error occurred at recursive SQL level 1 / ORA-01882: timezone region not found
		tz=getTimeZone();
		//var d1=tz.getDefault();
		tz.setDefault(tz);
		//throw d1&":"&tz.getDefault();

	 	this.datasource = {
			  class: 'oracle.jdbc.OracleDriver'
			, bundleName: 'ojdbc6'
			, bundleVersion: '11.2.0.4'
			, connectionString: 'jdbc:oracle:thin:@#orc.server#:#orc.port#/#orc.database#'
			, username: orc.username
			, password: orc.password
		};


		function onRequestStart(){
			setting showdebugOutput=false;
			//  create package
			query {
		        echo("CREATE OR REPLACE package lucee_bug_test as PROCEDURE testproc;
						PROCEDURE testproc2(p1 varchar2);
					end;"
				);
			}
			//  create package body
			query {
		        echo("CREATE OR REPLACE package body lucee_bug_test as
					PROCEDURE testproc IS
					BEGIN
						NULL;
					END;
						procedure testproc2(p1 varchar2) is
					begin
						null;
					end;
					END;"
				);
			}
			//  create Synonym for package
			query {
		        echo("create or replace synonym bu## for lucee_bug_test"
				);
			}
		}

		private struct function getCredencials() {
			var orc={};

			if(
				!isNull(server.system.environment.ORACLE_SERVER) && 
				!isNull(server.system.environment.ORACLE_USERNAME) && 
				!isNull(server.system.environment.ORACLE_PASSWORD) && 
				!isNull(server.system.environment.ORACLE_PORT) && 
				!isNull(server.system.environment.ORACLE_DATABASE)
			) {
				// getting the credentials from the environment variables
				orc.server=server.system.environment.ORACLE_SERVER;
				orc.username=server.system.environment.ORACLE_USERNAME;
				orc.password=server.system.environment.ORACLE_PASSWORD;
				orc.port=server.system.environment.ORACLE_PORT;
				orc.database=server.system.environment.ORACLE_DATABASE;
			} else if(
				// getting the credentials from the system variables
				!isNull(server.system.properties.ORACLE_SERVER) && 
				!isNull(server.system.properties.ORACLE_USERNAME) && 
				!isNull(server.system.properties.ORACLE_PASSWORD) && 
				!isNull(server.system.properties.ORACLE_PORT) && 
				!isNull(server.system.properties.ORACLE_DATABASE)
			) {
				orc.server=server.system.properties.ORACLE_SERVER;
				orc.username=server.system.properties.ORACLE_USERNAME;
				orc.password=server.system.properties.ORACLE_PASSWORD;
				orc.port=server.system.properties.ORACLE_PORT;
				orc.database=server.system.properties.ORACLE_DATABASE;
			}
			return orc;
		}
	</cfscript>
</cfcomponent>