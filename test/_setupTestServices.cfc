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

				// hack! manually set up the variables, 6.0 handles this differently
				var System = createObject("java", "java.lang.System");
					for ( var p in server.custom_build_env ){
						if ( len( server.custom_build_env[ p ] ) gt 0 ) // skip empty
							System.setProperty( p, server.custom_build_env[ p ] );
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
			"MONGODB_DATABASE": "lucee",
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
			"SMTP_PORT_INSECURE": 587,

			"MEMCACHED_SERVER": "localhost",
			// "MEMCACHED_PORT": 11211 // DON'T COMMIT

			"REDIS_SERVER": "localhost",
			// "REDIS_PORT": 6379 // DON'T COMMIT
			
		};
	}

	public void function loadServiceConfig() localmode=true {
		systemOutput( "", true) ;
		systemOutput("-------------- Test Services ------------", true );

		loop list="MySQL,MSsql,postgres,h2,oracle,mongoDb,smtp,pop,imap,s3,ftp,sftp,memcached,redis,ldap" item="service" {
			cfg = server.getTestService( service=service, verify=true );
			server.test_services[ service ]= {
				valid: false,
				missedTests: 0
			};
			
			if ( StructCount(cfg) eq 0 ){
				systemOutput( "Service [ #service# ] not configured", true) ;
			} else {
				// validate the cfg
				verify = "configured, but not tested";
				try {
					switch ( service ){
						case "s3":
							verify = verifyS3(cfg);
							break;
						case "imap":
							verify = verifyImap(cfg);
							break;
						case "pop":
							break;
						case "smtp":
							break;
						case "ftp":
							verify = verifyFTP(cfg, service);
							break;
						case "sftp":
							verify = verifyFTP(cfg, service);
							break;
						case "mongoDb":
							verify = verifyMongo(cfg);
							break;
						case "memcached":
							verify = verifyMemcached(cfg);
							break;
						case "redis":
							verify = verifyRedis(cfg);
							break;
						case "ldap":
							verify = verifyLDAP(cfg);
							break;
						default:
							verify = verifyDatasource(cfg);
							break;
					}
					systemOutput( "Service [ #service# ] is [ #verify# ]", true) ;
					server.test_services[service].valid = true;
				} catch (e) {
					st = test._testRunner::trimJavaStackTrace(cfcatch.stacktrace);
					systemOutput( "ERROR Service [ #service# ] threw [ #arrayToList(st, chr(10) )# ]", true);
					server.test_services[service].stacktrace = st;
				}
			}
		}
		systemOutput( " ", true);
	}

	public array function reportServiceSkipped () localmode=true {
		skipped = [];
		for ( s in server.test_services ){
			service = server.test_services[ s ];
			if ( !service.valid && service.missedTests gt 0 ){
				ArrayAppend( skipped, "-> Service [ #s# ] #chr(9)# not available, #chr(9)# #service.missedTests# tests skipped" );
			}
		}
		return skipped;
	}

	public array function reportServiceFailed() localmode=true {
		failed = [];
		for ( s in server.test_services ){
			service = server.test_services[ s ];
			if ( !service.valid and structKeyExists( service, "stacktrace" ) ){
				ArrayAppend( failed, "-> Service [ #s# ] #chr(9)# threw" );
				for ( st in service.stacktrace ) {
					ArrayAppend( failed, st );
				}
			}
		}
		return failed;
	}
	
	public boolean function failOnConfiguredServiceError() localmode=true{
		buildCfg = server._getSystemPropOrEnvVars( "LUCEE_BUILD_FAIL_CONFIGURED_SERVICES_FATAL", "", false );
		return buildCfg.LUCEE_BUILD_FAIL_CONFIGURED_SERVICES_FATAL ?: false;
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

	public function verifyMemcached ( memcached ) localmode=true{
		if ( structCount( memcached ) eq 2 ){
			if ( !isRemotePortOpen( memcached.server, memcached.port ) )
				throw "MemCached port closed #memcached.server#:#memcached.port#"; // otherwise the cache keeps trying and logging
			try {
				testCacheName = "testMemcached";
				application 
					action="update" 
					caches="#{
						testMemcached: {
							class: 'org.lucee.extension.cache.mc.MemcachedCache'
							, bundleName: 'memcached.extension'
							, bundleVersion: '4.0.0.10-SNAPSHOT'
							, storage: false
							, custom: {
								"socket_timeout": "3",
								"initial_connections": "1",
								"alive_check": "true",
								"buffer_size": "1",
								"max_spare_connections": "32",
								"storage_format": "Binary",
								"socket_connect_to": "3",
								"min_spare_connections": "1",
								"maint_thread_sleep": "5",
								"failback": "true",
								"max_idle_time": "600",
								"max_busy_time": "30",
								"nagle_alg": "true",
								"failover": "false",
								"servers": "#memcached.server#:#memcached.port#"
							}
							, default: ''
						}
					}#";
				cachePut( id='abcd', value=1234, cacheName=testCacheName );
				valid = !isNull( cacheGet( id:'abcd', cacheName:testCacheName ) );
				application action="update" caches="#{}#";
				if ( !valid ) {
					throw "MemCached configured, but not available";
				} else {
					return "MemCached connection verified";
				}
			} catch (e){
				application action="update" caches="#{}#";
				rethrow;
			}
		}
		throw "not configured";
	}	

	public function verifyRedis ( redis ) localmode=true{
		if ( structCount( redis ) eq 2 ){
			return "configured (not tested)";
		}	
		throw "not configured";
	}

	public function verifyImap ( imap ) localmode=true{
		imap
			action="open" 
			server = imap.SERVER
			username = imap.USERNAME
			port = imap.PORT_INSECURE
			secure="no"
			password = imap.PASSWORD
			connection = "testImap";
		imap
			action = "close",
			connection="testImap";
			
		return "configured";
	}

	public function verifyLDAP ( ldap ) localmode=true {
		cfldap( server=ldap.server,
			port=ldap.port,
			timeout=5000,
			username=ldap.username,
			password=ldap.password,
			action="query",
			name="local.results",
			start=ldap.base_dn,
			filter="(objectClass=inetOrgPerson)",
			attributes="cn" );
		return "configured";
	}

	public function addSupportFunctions() {
		server._getTempDir = function ( string prefix="" ) localmode=true{
			if ( len( arguments.prefix ) eq 0 ) {
				local.dir = getTempDirectory() & "lucee-tests\" & createGUID();
			} else {
				local.dir = getTempDirectory() & "lucee-tests\" & arguments.prefix;
			}
			if ( !directoryExists( dir ) )
				directoryCreate( dir, true );
			return dir;
		};
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
	public struct function getTestService( required string service, 
			string dbFile="", 
			boolean verify=false, 
			boolean onlyConfig=false 
		) localmode=true {
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
				mssql = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "MSSQL_");
				if ( structCount( msSql ) gt 0){
					if ( arguments.onlyConfig )
						return msSql;
					return {
						class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
						, bundleName: 'org.lucee.mssql'
						, bundleVersion: server.getDefaultBundleVersion('org.lucee.mssql', '12.2.0.jre8')
						, connectionString: 'jdbc:sqlserver://#msSQL.SERVER#:#msSQL.PORT#;DATABASENAME=#msSQL.DATABASE#;sendStringParametersAsUnicode=true;SelectMethod=direct;trustServerCertificate=true'
						, username: msSQL.username
						, password: msSQL.password
					};
				}
				break;
			case "mysql":
				mysql = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "MYSQL_");	
				if ( structCount( mySql ) gt 0 ){
					if ( arguments.onlyConfig )
						return mySql;
					return {
						class: 'com.mysql.cj.jdbc.Driver'
						, bundleName: 'com.mysql.cj'
						, bundleVersion: server.getDefaultBundleVersion('com.mysql.cj', '8.0.33')
						, connectionString: 'jdbc:mysql://#mySQL.server#:#mySQL.port#/#mySQL.database#?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true&useSSL=false'
						, username: mySQL.username
						, password: mySQL.password
					};
				}
				break;
			case "postgres":
				pgsql = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "POSTGRES_");	
				if ( structCount( pgsql ) gt 0 ){
					if ( arguments.onlyConfig )
						return pgsql;
					return {
						class: 'org.postgresql.Driver'
						, bundleName: 'org.postgresql.jdbc'
						, bundleVersion: server.getDefaultBundleVersion('org.postgresql.jdbc', '42.6.0')
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
				if ( len( arguments.dbFile ) ){
					return {
						class: 'org.h2.Driver'
						, bundleName: 'org.lucee.h2'
						, bundleVersion: server.getDefaultBundleVersion('org.lucee.h2', '2.1.214.0001L')
						, connectionString: 'jdbc:h2:#arguments.dbFile#/datasource/db;MODE=MySQL'
					};
				}
				break;
			case "hsqldb":
				if ( arguments.verify ){
					tempDb = "#getTempDirectory()#/hsqldb-#createUUID()#";
					if (! DirectoryExists( tempDb ) )
						DirectoryCreate( tempDb );
					arguments.dbFile = tempDb;
				}
				if ( len( arguments.dbFile ) ){
					return {
						class: 'org.hsqldb.jdbcDriver'
						, bundleName: 'org.hsqldb.hsqldb'
						, bundleVersion: server.getDefaultBundleVersion( 'org.lucee.hsqldb', '2.7.2.jdk8' )
						, connectionString: 'jdbc:hsqldb:#arguments.dbFile#/datasource/db;MODE=MySQL'
					};
				}
				break;
			case "mongoDB":
				mongoDB = server._getSystemPropOrEnvVars( "SERVER, PORT, DB", "MONGODB_");
				mongoDBcreds = server._getSystemPropOrEnvVars( "USERNAME, PASSWORD", "MONGODB_");
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
				oracle = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "ORACLE_");	
				if ( structCount( oracle ) gt 0 ){
					if ( arguments.onlyConfig )
						return oracle;
					return {
						class: 'oracle.jdbc.OracleDriver'
						, bundleName: 'ojdbc6'
						, bundleVersion: server.getDefaultBundleVersion('ojdbc6', '11.2.0.4')
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
			case "memcached":
				memcached = server._getSystemPropOrEnvVars( "SERVER, PORT", "MEMCACHED_" );
				if ( memcached.count() eq 2 ){
					return memcached;
				}
				break;
			case "redis":
				redis = server._getSystemPropOrEnvVars( "SERVER, PORT", "REDIS_" );
				if ( redis.count() eq 2 ){
					return redis;
				}
				break;
			case "ldap":
				ldap = server._getSystemPropOrEnvVars( "SERVER, PORT, USERNAME, PASSWORD, BASE_DN", "LDAP_" );
				if ( ldap.count() eq 5 ){
					return ldap;
				}
				break;
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
			systemOutput("getDefaultBundleVersion: [" & arguments.bundleName & "] FALLLING BACK TO DEFAULT [" & arguments.fallbackVersion & "]", true)
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

	function isTestServiceAllowed( service ){
		if ( len( request.testServices) eq 0 )
			return true;
		loop list=request.testServices item="local.testService" {
			if ( local.testService eq arguments.service )
				return true;
		}
		return false;
	}

	boolean function isRemotePortOpen( string host, numeric port, numeric timeout=2000 ) {
		var socket = createObject( "java", "java.net.Socket").init();
		var address = createObject( "java", "java.net.InetSocketAddress" ).init(
			javaCast( "string", arguments.host ),
			javaCast( "int", arguments.port )
		);

		try {
			socket.connect( address, javaCast( "int", arguments.timeout ));
			socket.close();
			return true;
		} catch (e) {
			return false;
		}
	}
}


