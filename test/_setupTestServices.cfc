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
		"S3_SECRET_KEY": "top-secret",
		"MONGODB_PORT": 27017,
		"MEMCACHED_PORT": 11211,
		"UPDATE_PROVIDER_URL": http://update.localhost"
	}

	then add an ENV var pointing to the .json file
	
	LUCEE_BUILD_ENV=c:\work\lucee_build_env.json"

	You can also pass "-DLUCEE_BUILD_ENV=c:/work/lucee_build_env.json" directly to ANT
	to have it passed to the JVM.
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
			// "MONGODB_PORT": 27017, // DON'T COMMIT
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

			"SFTP_SERVER": "127.0.0.1",
			"SFTP_USERNAME": "lucee",
			"SFTP_PASSWORD": "",  // DON'T COMMIT
			"SFTP_PORT": 22,
			"SFTP_BASE_PATH": "/",

			"FTPS_SERVER": "127.0.0.1",
			"FTPS_USERNAME": "lucee",
			"FTPS_PASSWORD": "",  // DON'T COMMIT
			"FTPS_PORT": 990,
			"FTPS_BASE_PATH": "/",
			
			"S3_ACCESS_KEY_ID": "",
			"S3_SECRET_KEY": "", // DON'T COMMIT
			"S3_BUCKET_PREFIX": "lucee-ldev-",

			"S3_CUSTOM_ACCESS_KEY_ID": "",
			"S3_CUSTOM_SECRET_KEY": "", // DON'T COMMIT
			"S3_CUSTOM_HOST": "http://localhost:9000", // i.e. minio
			"S3_CUSTOM_BUCKET_PREFIX": "lucee-ldev-",

			"S3_GOOGLE_ACCESS_KEY_ID": "",
			"S3_GOOGLE_SECRET_KEY": "", // DON'T COMMIT
			"S3_GOOGLE_HOST": "storage.googleapis.com",
			"S3_GOOGLE_BUCKET_PREFIX": "lucee-ldev-",
			// imap, pop and smtp rely on MAIL_PASSWORD being defined

			"IMAP_SERVER": "localhost",
			"IMAP_PORT_SECURE": 993,
			"IMAP_PORT_INSECURE": 143,
			"IMAP_USERNAME": "lucee",
			"IMAP_PASSWORD": "", // DON'T COMMIT

			"POP_SERVER": "localhost",
			"POP_PORT_SECURE": 995,
			"POP_PORT_INSECURE": 110,
			"POP_USERNAME": "lucee",
			"POP_PASSWORD": "", // DON'T COMMIT

			"SMTP_SERVER": "localhost",
			"SMTP_PORT_SECURE": 25,
			"SMTP_PORT_INSECURE": 587,
			"SMTP_USERNAME": "lucee",
			"SMTP_PASSWORD": "", // DON'T COMMIT

			"MEMCACHED_SERVER": "localhost",
			// "MEMCACHED_PORT": 11211 // DON'T COMMIT

			"REDIS_SERVER": "localhost",
			// "REDIS_PORT": 6379 // DON'T COMMIT
			"LDAP_SERVER": "localhost"
			// "LDAP_USERNAME":
			// "LDAP_PASSWORD":
			// "LDAP_PORT":  10389 // DON't COMMMIT
			// "LDAP_BASE_DN": "dc=example"

		};
	}

	public void function loadServiceConfig() localmode=true {
		systemOutput( "", true) ;
		systemOutput("-------------- Test Services ------------", true );
		services = ListToArray("oracle,MySQL,MSsql,postgres,h2,mongoDb,smtp,pop,imap,s3,s3_custom,s3_google,s3_backblaze,ftp,sftp,memcached,redis,ldap");
		// can take a while, so we check them them in parallel

		services.each( function( service ) localmode=true {
			if (! isTestServiceAllowed( arguments.service )){
				systemOutput( "Service [ #arguments.service# ] disabled, not found in testServices", true) ;
				server.test_services[arguments.service] = {
					valid: false,
					missedTests: 0
				};
				return;
			}
			cfg = server.getTestService( service=arguments.service, verify=true );
			server.test_services[ arguments.service ]= {
				valid: false,
				missedTests: 0
			};
			if ( StructCount(cfg) eq 0 ){
				systemOutput( "Service [ #arguments.service# ] not configured", true) ;
				if ( len( request.testServices) gt 0 ){
					systemOutput( "Requested Test Service [ #arguments.service# ] not available", true);
					throw "Requested Test Service [ #arguments.service# ] not available";
				}
			} else {
				// validate the cfg
				verify = "configured, but not tested";
				try {
					switch ( arguments.service ){
						case "s3":
							verify = verifyS3(cfg);
							break;
						case "s3_custom":
							verify = verifyS3Custom(cfg);
							break;
						case "s3_google":
							verify = verifyS3Custom(cfg);
							break;
						case "s3_backblaze":
							verify = verifyS3Custom(cfg);
							break;
						case "imap":
							verify = verifyImap(cfg);
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
						case "ftps":
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
					systemOutput( "Service [ #arguments.service# ] is [ #verify# ]", true) ;
					server.test_services[arguments.service].valid = true;
				} catch (e) {
					st = test._testRunner::trimJavaStackTrace( cfcatch.stacktrace );
					if ( isEmpty( st ) or ( arrayLen( st ) eq 1 and trim( st [ 1 ] ) eq "" ) )
						st = [ cfcatch.message ];
					systemOutput( "ERROR Service [ #arguments.service# ] threw [ #arrayToList(st, chr(10))# ]", true);
					if ( cfcatch.message contains "NullPointerException" || request.testDebug )
						systemOutput(cfcatch, true);
					if ( len( request.testServices) gt 0 ){
						systemOutput( "Requested Test Service [ #arguments.service# ] not available", true);
						systemOutput(cfcatch, true);
						throw "Requested Test Service [ #arguments.service# ] not available";
					}
					server.test_services[arguments.service].stacktrace = st;
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
				ArrayAppend( failed, "-> Service [ #s# ] #chr( 9 )# threw" );
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
		opts = {
			"connectTimeoutMS": 2500,
			"serverSelectionTimeoutMS": 2500
		}; // default is 30s, which slows down tests when not available

		// neither of these two work
		conn = MongoDBConnect("#arguments.mongo.server#:#arguments.mongo.port#/#arguments.mongo.db#?#opts#);
		conn = MongoDBConnect("#arguments.mongo.server#:#arguments.mongo.port#/#arguments.mongo.db#", opts);
		*/

		/*
		var q = extensionList().filter(function(row){
			return row.name contains "mongo";
		});
		*/
		// systemOutput(conn.command("buildInfo"));
		name = conn.command("buildInfo").version; // & ", " & q.name;
		//conn.disconnect();
		return "MongoDB " & name;
	}

	public function verifyFTP ( ftp, service ) localmode=true {
		if  ( arguments.service eq "ftps" )
			secure = "ftps";
		else
			secure = ( arguments.service );
		ftp action = "open" 
			connection = "checkConn" 
			timeout = 2
			secure= secure
			username = arguments.ftp.username
			password = arguments.ftp.password
			server = arguments.ftp.server
			port= arguments.ftp.port;

		//SystemOutput(cfftp, true);
		if ( !cfftp.succeeded )
			throw cfftp.errorText;
		sig = cfftp.returnValue.trim(); // stash, close changes cfftp
		ftp action = "close" connection = "checkConn";
		
		return sig & ", #arguments.ftp.username#@#arguments.ftp.server#:#arguments.ftp.port#";
	}

	public function verifyS3 ( s3 ) localmode=true{
		bucketName = arguments.s3.BUCKET_PREFIX & "verify";
		base = "s3://#arguments.s3.ACCESS_KEY_ID#:#arguments.s3.SECRET_KEY#@/#bucketName#";
		try {
			directoryExists( base );
		} catch ( e ){
			throw listFirst( replaceNoCase( e.message, arguments.s3.SECRET_KEY, "***", "all" ), "." );
		}
		return "s3 Connection Verified [#bucketName#]";
	}

	public function verifyS3Custom ( s3 ) localmode=true{
		bucketName = arguments.s3.BUCKET_PREFIX & "verify";
		base = "s3://#arguments.s3.ACCESS_KEY_ID#:#arguments.s3.SECRET_KEY#@#arguments.s3.HOST#/#bucketName#";
		try {
			if ( ! directoryExists( base ) )
				directoryCreate( base ); // for GHA, the local service starts empty
		} catch ( e ) {
			throw listFirst( replaceNoCase( e.message, arguments.s3.SECRET_KEY, "***", "all" ), "." );
		}
		return "s3 custom Connection verified [#bucketName#]";
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
		if ( structCount( LDAP ) eq 6 ){
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
		throw "not configured";
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
							kk = mid(k, len( arguments.prefix ) + 1 ); // i.e. return DATABASE for MSSQL_DATABASE
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
			boolean onlyConfig=false,
			string connectionString="",
			struct options={}
		) localmode=true {

		if ( StructKeyExists( server.test_services, arguments.service ) ){
			if ( !server.test_services[ arguments.service ].valid ){
				SystemOutput("Warning service: [ #arguments.service# ] is not available", true);
				if ( !arguments.verify )
					server.test_services[ arguments.service ].missedTests++;
				return {};
			}
		}

		switch ( arguments.service ){
			case "updateProvider":
				updateProvider = server._getSystemPropOrEnvVars( "URL", "UPDATE_PROVIDER_" );
				if ( structCount( updateProvider ) eq 1 ){
					return updateProvider;
				} else {
					return {url: "https://update.lucee.org" };
				}
			case "mssql":
				mssql = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "MSSQL_" );
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
					}.append( arguments.options );
				}
				break;
			case "mysql":
				mysql = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "MYSQL_" );	
				if ( structCount( mySql ) gt 0 ){
					if ( arguments.onlyConfig )
						return mySql;
					return {
						class: 'com.mysql.cj.jdbc.Driver'
						, bundleName: 'com.mysql.cj'
						, bundleVersion: server.getDefaultBundleVersion( 'com.mysql.cj', '9.1.0' )
						, connectionString: 'jdbc:mysql://#mySQL.server#:#mySQL.port#/#mySQL.database#?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true&useSSL=false' & arguments.connectionString
						, username: mySQL.username
						, password: mySQL.password
					}.append( arguments.options );
				}
				break;
			case "postgres":
				pgsql = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "POSTGRES_" );	
				if ( structCount( pgsql ) gt 0 ){
					if ( arguments.onlyConfig )
						return pgsql;
					return {
						class: 'org.postgresql.Driver'
						, bundleName: 'org.postgresql.jdbc'
						, bundleVersion: server.getDefaultBundleVersion( 'org.postgresql.jdbc', '42.7.3' )
						, connectionString: 'jdbc:postgresql://#pgsql.server#:#pgsql.port#/#pgsql.database#' & arguments.connectionString
						, username: pgsql.username
						, password: pgsql.password
					}.append( arguments.options );
				}
				break;
			case "h2":
				if ( arguments.verify ){
					tempDb = server._getTempDir("h2-verify");
					if (! DirectoryExists( tempDb ) )
						DirectoryCreate( tempDb );
					arguments.dbFile = tempDb;
				}
				if ( len( arguments.dbFile ) ){
					return {
						class: 'org.h2.Driver'
						, bundleName: 'org.lucee.h2'
						, bundleVersion: server.getDefaultBundleVersion( 'org.lucee.h2', '2.1.214.0001L' )
						, connectionString: 'jdbc:h2:#arguments.dbFile#/db;MODE=MySQL' & arguments.connectionString
					}.append( arguments.options );
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
						, bundleName: 'org.lucee.hsqldb'
						, bundleVersion: server.getDefaultBundleVersion( 'org.lucee.hsqldb', '2.7.2.jdk8' )
						, connectionString: 'jdbc:hsqldb:#arguments.dbFile#/datasource/db;MODE=MySQL'
					};
				}
				break;
			case "mongoDB":
				mongoDB = server._getSystemPropOrEnvVars( "SERVER, PORT, DB", "MONGODB_" );
				mongoDBcreds = server._getSystemPropOrEnvVars( "USERNAME, PASSWORD", "MONGODB_" );
				if ( structCount( mongoDb ) eq 3 ){
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
					if ( arguments.onlyConfig )
						return oracle;
					return {
						class: 'oracle.jdbc.OracleDriver'
						, bundleName: 'org.lucee.oracle'
						, bundleVersion: server.getDefaultBundleVersion( 'org.lucee.oracle', '19.17.0.0-ojdbc8' )
						, connectionString: 'jdbc:oracle:thin:@#oracle.server#:#oracle.port#/#oracle.database#' & arguments.connectionString
						, username: oracle.username
						, password: oracle.password
					}.append( arguments.options );
				}
				break;
			case "ftp":
				ftp = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, BASE_PATH", "FTP_");
				return ftp;	
			case "sftp":
				sftp = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, BASE_PATH", "SFTP_");
				return sftp;
			case "ftps":
				ftps = server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, BASE_PATH", "FTPS_");
				return ftps;
			case "smtp":
				smtp = server._getSystemPropOrEnvVars( "SERVER, PORT_SECURE, PORT_INSECURE, USERNAME, PASSWORD", "SMTP_" );
				return smtp;
			case "imap":
				imap = server._getSystemPropOrEnvVars( "SERVER, PORT_SECURE, PORT_INSECURE, USERNAME, PASSWORD", "IMAP_" );
				return imap;
			case "pop":
				pop = server._getSystemPropOrEnvVars( "SERVER, PORT_SECURE, PORT_INSECURE, USERNAME, PASSWORD", "POP_" );
				return pop;
			case "s3":
				s3 = server._getSystemPropOrEnvVars( "ACCESS_KEY_ID, SECRET_KEY, BUCKET_PREFIX", "S3_" );
				return s3;
			case "s3_custom":
				s3 = server._getSystemPropOrEnvVars( "ACCESS_KEY_ID, SECRET_KEY, HOST, BUCKET_PREFIX", "S3_CUSTOM_" );
				return s3;
			case "s3_google":
				s3 = server._getSystemPropOrEnvVars( "ACCESS_KEY_ID, SECRET_KEY, HOST, BUCKET_PREFIX", "S3_GOOGLE_" );
				return s3;
			case "s3_backblaze":
				s3 = server._getSystemPropOrEnvVars( "ACCESS_KEY_ID, SECRET_KEY, HOST, BUCKET_PREFIX", "S3_BACKBLAZE_" );
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
				ldap = server._getSystemPropOrEnvVars( "SERVER, PORT, PORT_SECURE, USERNAME, PASSWORD, BASE_DN", "LDAP_" );
				if ( ldap.count() eq 6 ){
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
			//systemOutput( "getDefaultBundleVersion: [" & arguments.bundleName & "] FALLLING BACK TO DEFAULT [" & arguments.fallbackVersion & "]", true );
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

