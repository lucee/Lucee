component {
	/*

	rather than using env vars, you can define servers for testing here, just set a password etc

	note, a server configuration is only considered valid when all the requested properties have a value 
	empty strings are ignored, so the following configs will be ignored due to *_PASSWORD being empty

	i.e. a env.json
	{
		"MSSQL_PASSWORD": "top-secret",
		"MYSQL_PASSWORD": "top-secret",
		"POSTGRES_PASSWORD": "top-secret",
		"MONGODB_PASSWORD": "top-secret",
		"ORACLE_PASSWORD": "top-secret",
		"FTP_PASSWORD": "top-secret",
		"SFTP_PASSWORD": "top-secret",
		"MAIL_PASSWORD": "top-secret",
		"S3_SECRET_KEY": "top-secret"
	}

	*/

	public function init (){
		if ( !server.keyExists( "test_services" ) )
			server.test_services = {};
		return this;
	}

	public function setup () {
		addSupportFunctions(); // puts functions into server scope so they can be accessed by all tests;
		loadCustomBuildEnv();
		loadServiceConfig();
	}

	public void function loadCustomBuildEnv() localmode=true {
		loadCustomEnvStubs();
		build_cfg =  server._getSystemPropOrEnvVars( "LUCEE_BUILD_ENV", "", false );

		if ( structKeyExists( build_cfg, "LUCEE_BUILD_ENV" ) ){
			env = build_cfg.LUCEE_BUILD_ENV;
			if (!FileExists(env)){
				systemOutput( "ERROR: LUCEE_BUILD_ENV [#env#] file doesn't exist", true);
			} else {
				f = fileRead( env );
				if ( isJson( f ) ){
					structAppend( server.custom_build_env, deserializeJson( f ) );
					systemOutput( "IMPORTED: LUCEE_BUILD_ENV [#env#] configuation ", true );
				} else {
					systemOutput( "ERROR: LUCEE_BUILD_ENV [#env#] wasn't json", true );
				}
			}
		}
	}

	public function loadCustomEnvStubs(){
		server.custom_build_env = {
			"MSSQL_SERVER": "127.0.0.1",
			"MSSQL_USERNAME": "lucee",
			"MSSQL_PASSWORD": "", // DON'T COMMIT
			"MSSQL_PORT": 1433,
			"MSSQL_DATABASE": "lucee", 

			"MYSQL_SERVER": "localhost",
			"MYSQL_USERNAME": "lucee",
			"MYSQL_PASSWORD": "",  // DON'T COMMIT
			"MYSQL_PORT": 3306,
			"MYSQL_DATABASE": "lucee",

			"POSTGRES_SERVER": "localhost",
			"POSTGRES_USERNAME": "lucee",
			"POSTGRES_PASSWORD": "",  // DON'T COMMIT
			"POSTGRES_PORT": 5432,
			"POSTGRES_DATABASE": "lucee",

			"MONGODB_SERVER": "localhost",
			"MONGODB_USERNAME": "",
			"MONGODB_PASSWORD": "",  // DON'T COMMIT
			"MONGODB_PORT": 27017,
			"MONGODB_DB": "lucee",
			/*
			-- USER SQL
				CREATE USER "C##LUCEE" INDENTIFIED BY "LUCEE"
				ALTER USER "C##LUCEE"
					DEFAULT TABLESPACE "USERS"
					TEMPORARY TABLESPACE "TEMP" 
					ACCOUNT UNLOCK ;

				-- QUOTAS
				ALTER USER "C##LUCEE" QUOTA UNLIMITED ON "USERS";

				-- ROLES
				ALTER USER "C##LUCEE" DEFAULT ROLE "CONNECT, RESOURCE";
			*/
			"ORACLE_SERVER": "localhost",
			"ORACLE_USERNAME": "c####lucee",
			"ORACLE_PASSWORD": "",  // DON'T COMMIT
			"ORACLE_PORT": 1521,
			"ORACLE_DATABASE": "XE",
			
			"FTP_SERVER": "localhost",
			"FTP_USERNAME": "lucee",
			"FTP_PASSWORD": "",  // DON'T COMMIT
			"FTP_PORT": 21,
			"FTP_BASE_PATH": "/",

			"SFTP_SERVER"="localhost",
			"SFTP_USERNAME": "lucee",
			"SFTP_PASSWORD": "",  // DON'T COMMIT
			"SFTP_PORT": 990,
			"SFTP_BASE_PATH": "/",
			
			"S3_ACCESS_KEY_ID": "test",
			"S3_SECRET_KEY": "",

			"MAIL_USERNAME": "lucee",
			"MAIL_PASSWORD": "", // DON'T COMMIT

			// imap, pop and smtp rely on MAIL_PASSWORD being defined

			"IMAP_SERVER": "localhost",
			"IMAP_PORT_SECURE": 993,
			"IMAP_PORT_INSECURE": 143,

			"POP_SERVER": "localhost",
			"POP_PORT_SECURE": 995,
			"POP_PORT_INSECURE": 110,

			"SMTP_SERVER": "localhost",
			"SMTP_PORT_SECURE": 25,
			"SMTP_PORT_INSECURE": 587
		};
	}

	public void function loadServiceConfig() localmode=true {
		systemOutput( "", true) ;		
		systemOutput("-------------- Test Services ------------", true );

		services = ListToArray("oracle,MySQL,MSsql,postgres,h2,mongoDb,smtp,pop,imap,s3,ftp,sftp");
		// take a while, do them in parallel
		services.each( function( service ) localmode=true {
			cfg = server.getTestService( service=arguments.service, verify=true );
			server.test_services[ arguments.service ]= {
				valid: false,
				missedTests: 0
			};
			if ( StructCount(cfg) eq 0 ){
				systemOutput( "Service [ #arguments.service# ] not configured", true) ;
			} else {
				// validate the cfg
				verify = "configured, but not tested";
				try {
					switch ( arguments.service ){
						case "s3":
							verify = verifyS3(cfg);
							break;
						case "imap":
							break;
						case "pop":
							break;
						case "smtp":
							break;
						case "ftp":
							verify = verifyFTP(cfg, arguments.service);
							break;
						case "sftp":
							verify = verifyFTP(cfg, arguments.service);
							break;
						case "mongoDb":
							verify = verifyMongo(cfg);
							break;
						default:
							verify = verifyDatasource(cfg);
							break;
					}
					systemOutput( "Service [ #arguments.service# ] is [ #verify# ]", true) ;
					server.test_services[arguments.service].valid = true;
				} catch (e) {
					systemOutput( "ERROR Service [ #arguments.service# ] threw [ #cfcatch.message# ]", true);
				}
			}
		}, true, 4);
		systemOutput( " ", true);
	}

	public array function reportServiceSkipped () localmode=true {
		skipped = [];
		for (s in server.test_services ){
			service = server.test_services[s];
			if ( !service.valid && service.missedTests gt 0 ){
				ArrayAppend( skipped, "-> Service [#s#] #chr(9)# not available, #chr(9)# #service.missedTests# tests skipped" );
			}
		}
		return skipped;
	}

	public string function verifyDatasource ( struct datasource ) localmode=true{
		dbinfo type="Version" datasource="#arguments.datasource#" name="verify";
		dbDesc = [];
		loop list="#verify.columnlist#" item="col" {
			ArrayAppend( dbDesc, verify[ col ] );
		}
		return ArrayToList( dbDesc, ", " );
	}
	
	public function verifyMongo ( mongo ) localmode=true {
		conn = MongoDBConnect( arguments.mongo.db, arguments.mongo.server, arguments.mongo.port );
		/*
		var q = extensionList().filter(function(row){
			return row.name contains "mongo";
		});
		*/
		name = conn.command("buildInfo").version; // & ", " & q.name;
		//conn.disconnect();
		return "MongoDB " & name;
	}

	public function verifyFTP ( ftp, service ) localmode=true {
		ftp action = "open" 
			connection = "conn" 
			timeout = 5
			secure= (arguments.service contains "sftp")
			username = arguments.ftp.username
			password = arguments.ftp.password
			server = arguments.ftp.server
			port= arguments.ftp.port;
		
		//ftp action = "close" connection = "conn";
		
		return "Connection Verified";
	}

	public function verifyS3 ( s3 ) localmode=true{
		bucketName = "lucee-testsuite";
		base = "s3://#arguments.s3.ACCESS_KEY_ID#:#arguments.s3.SECRET_KEY#@/#bucketName#";
		DirectoryExists( base );		
		return "s3 Connection Verified";
	}
		

	public function addSupportFunctions() {
		server._getSystemPropOrEnvVars = function ( string props="", string prefix="", boolean stripPrefix=true, boolean allowEmpty=false ) localmode=true{
			st = [=];
			keys = arguments.props.split( "," );
			n = arrayLen( keys ) ;
			loop list="custom,environment,properties" item="src" {
				if ( src eq "custom" )
					props = server.custom_build_env; // server.system is readonly
				else
					props = server.system[ src ];
				for (k in keys){
					k = prefix & trim( k );
					if ( !isNull( props[ k ] ) && Len( Trim( props[ k ] ) ) neq 0 ){
						kk = k;
						if ( arguments.stripPrefix )
							kk = ListRest( k, "_" ); // return DATABASE for MSSQL_DATABASE
						st[ kk ] = props[ k ];
					}
				}
				if ( structCount( st ) eq n )
					break;
				else 
					st = {};
			}
			if ( structCount( st ) eq n ){
				//systemOutput( st, true);
				return st; // all or nothing
			} else {
				return {};
			}
		};

		// use this rather than all the boilerplate
		server.getDatasource = getTestService;
		server.getTestService = getTestService;
		// TODO hmmmf closures and this scope!
		server.getDefaultBundleVersion = getDefaultBundleVersion;  
		server.getBundleVersions = getBundleVersions;
	}
	public struct function getTestService( required string service, string dbFile="", boolean verify=false ) localmode=true {
		if ( StructKeyExists( server.test_services, arguments.service ) ){
			if ( !server.test_services[ arguments.service ].valid ){
				//SystemOutput("Warning service: [ #arguments.service# ] is not available", true);
				if ( !arguments.verify )
					server.test_services[ arguments.service ].missedTests++;
				return {};
			}
		}

		switch ( arguments.service ){
			case "mssql":
				mssql = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "MSSQL_" );
				if ( structCount( msSql ) gt 0){
					return {
						class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
						, bundleName: 'org.lucee.mssql'
						, bundleVersion: server.getDefaultBundleVersion( 'org.lucee.mssql', '4.0.2206.100' )
						, connectionString: 'jdbc:sqlserver://#msSQL.SERVER#:#msSQL.PORT#;DATABASENAME=#msSQL.DATABASE#;sendStringParametersAsUnicode=true;SelectMethod=direct'
						, username: msSQL.username
						, password: msSQL.password
					};
				}
				break;
			case "mysql":
				mysql = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "MYSQL_" );	
				if ( structCount( mySql ) gt 0 ){
					return {
						class: 'com.mysql.cj.jdbc.Driver'
						, bundleName: 'com.mysql.cj'
						, bundleVersion: server.getDefaultBundleVersion( 'com.mysql.cj', '8.0.19' )
						, connectionString: 'jdbc:mysql://#mySQL.server#:#mySQL.port#/#mySQL.database#?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true&useSSL=false'
						, username: mySQL.username
						, password: mySQL.password
					};
				}
				break;
			case "postgres":
				pgsql = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "POSTGRES_" );	
				if ( structCount( pgsql ) gt 0 ){
					return {
						class: 'org.postgresql.Driver'
						, bundleName: 'org.postgresql.jdbc42'
						, bundleVersion: server.getDefaultBundleVersion( 'org.postgresql.jdbc42', '9.4.1212' )
						, connectionString: 'jdbc:postgresql://#pgsql.server#:#pgsql.port#/#pgsql.database#'
						, username: pgsql.username
						, password: pgsql.password
					};
				}
				break;
			case "h2":
				if ( arguments.verify ){
					tempDb = "#getTempDirectory()#/#createUUID()#";
					if (! DirectoryExists( tempDb ) )
						DirectoryCreate( tempDb );
					arguments.dbFile = tempDb;
				}
				if ( Len( arguments.dbFile ) ){
					return {
						class: 'org.h2.Driver'
						, bundleName: 'org.h2'
						, bundleVersion: server.getDefaultBundleVersion( 'org.h2', '1.3.172' )
						, connectionString: 'jdbc:h2:#arguments.dbFile#/datasource/db;MODE=MySQL'
					};
				}
				break;
			case "mongoDB":
				mongoDB = server._getSystemPropOrEnvVars( "SERVER, PORT, DB", "MONGODB_" );
				mongoDBcreds = server._getSystemPropOrEnvVars( "USERNAME, PASSWORD", "MONGODB_" );
				if ( structCount( mongoDb ) gt 0 ){
					if (structCount( mongoDBcreds ) eq 2 ){
						StructAppend(mongoDB, mongoDBcreds)
					} else {
						// _getSystemPropOrEnvVars ignores empty variables
						mongoDB.USERNAME="";
						mongoDB.PASSWORD="";
					}
					return mongoDB;
				}
				break;
			case "oracle":
				oracle = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "ORACLE_" );	
				if ( structCount( oracle ) gt 0 ){
					return {
						class: 'oracle.jdbc.OracleDriver'
						, bundleName: 'ojdbc7'
						, bundleVersion: server.getDefaultBundleVersion( 'ojdbc7', '11.2.0.4' )
						, connectionString: 'jdbc:oracle:thin:@#oracle.server#:#oracle.port#/#oracle.database#'
						, username: oracle.username
						, password: oracle.password
					};
				}
				break;
			case "ftp":
				ftp = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, BASE_PATH", "FTP_");
				return ftp;	
			case "sftp":
				sftp = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, BASE_PATH", "SFTP_");
				return sftp;
			case "mail":
				mail = server._getSystemPropOrEnvVars( "USERNAME, PASSWORD", "MAIL_" );
				return mail;
			case "smtp":
				mail = server._getSystemPropOrEnvVars( "USERNAME, PASSWORD", "MAIL_" );
				if ( mail.count() gt 0 ){
					smtp = server._getSystemPropOrEnvVars( "SERVER, PORT_SECURE, PORT_INSECURE", "SMTP_" );
					return smtp;
				}
				break;
			case "imap":
				mail = server._getSystemPropOrEnvVars( "USERNAME, PASSWORD", "MAIL_" );
				if ( mail.count() gt 0 ){
					imap = server._getSystemPropOrEnvVars( "SERVER, PORT_SECURE, PORT_INSECURE", "IMAP_" );
					return imap;
				}
				break;
			case "pop":
				mail = server._getSystemPropOrEnvVars( "USERNAME, PASSWORD", "MAIL_" );
				if ( mail.count() gt 0 ){
					pop = server._getSystemPropOrEnvVars( "SERVER, PORT_SECURE, PORT_INSECURE", "POP_" );
					return pop;
				}
				break;
			case "s3":
				s3 = server._getSystemPropOrEnvVars( "ACCESS_KEY_ID, SECRET_KEY", "S3_" );
				return s3;
			default:
				break;
		}
		//SystemOutput( "", true);
		SystemOutput( "Warning test service: [ #arguments.service# ] is not configured", true );
		return {};
	}


	function getDefaultBundleVersion( bundleName, fallbackVersion ) cachedWithin="request" {
		var bundles = server.getBundleVersions();
		if ( structKeyExists( bundles, arguments.bundleName ) ){
			//systemOutput(arguments.bundleName & " " & bundles[arguments.bundleName], true)
			return bundles[ arguments.bundleName ];
		} else {
			systemOutput( "getDefaultBundleVersion: [" & arguments.bundleName & "] FALLLING BACK TO DEFAULT [" & arguments.fallbackVersion & "]", true );
			return arguments.fallbackVersion ;
		}
	}		

	function getBundleVersions() cachedWithin="#createTimeSpan( 1, 0, 0, 0 )#"{
		admin 
			type="server"
			password="#server.SERVERADMINPASSWORD#" 
			action="getBundles" 
			returnvariable="q_bundles"
		var bundles = {};
		loop query=q_bundles {
			var _bundle = {};
			_bundle.append( q_bundles.headers );
			bundles[ _bundle[ 'Bundle-SymbolicName' ] ] = _bundle[ 'Bundle-Version' ];
		}
		return bundles;
	}
}

