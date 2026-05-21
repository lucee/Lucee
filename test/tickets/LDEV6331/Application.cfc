component {
	param name="url.sessionStorage" default="ram";
	param name="url.sessionCluster" default=false;
	param name="url.sessionCommitInterval" default="";
	// ttlSeconds is the session timeout used by the test. Default sessionCommitInterval = ttlSeconds/2.
	// Redis EXPIRE is seconds-precision, so 2s is the practical floor across all backends — bump if flaky on slow CI.
	param name="url.ttlSeconds" default="2";

	this.name = "ldev-6331-#url.sessionStorage#-cluster#url.sessionCluster#-" & hash( getCurrentTemplatePath() );
	this.sessionManagement = true;
	this.setClientCookies = true;
	this.sessionType = "application";
	this.sessionCluster = url.sessionCluster;
	this.sessionTimeout = createTimespan( 0, 0, 0, javacast( "int", url.ttlSeconds ) );
	this.applicationTimeout = createTimespan( 0, 1, 0, 0 );
	if ( len( url.sessionCommitInterval ) )
		this.sessionCommitInterval = createTimespan( 0, 0, 0, javacast( "int", url.sessionCommitInterval ) );

	if ( url.sessionStorage eq "redis" ) {
		// Redis honours per-put TTL (sessionTimeoutMs) as EXPIRE on the key.
		variables.redis = server.getTestService( "redis" );
		this.cache.connections[ "ldev6331cache" ] = {
			class: "lucee.extension.io.cache.redis.RedisCache",
			bundleName: "redis.extension",
			bundleVersion: server.getDefaultBundleVersion( "redis.extension", "4.0.1.1-SNAPSHOT" ),
			storage: true,
			custom: {
				"host": redis.server,
				"port": redis.port
			}
		};
		this.sessionStorage = "ldev6331cache";
	} else if ( url.sessionStorage eq "datasource" ) {
		// MySQL-backed session storage — covers LDEV-4670 (DB expires column never refreshed on read-only).
		variables.mysql = server.getDatasource( "mysql" );
		variables.mysql.storage = true;
		variables.datasourceName = "ldev6331-ds";
		this.datasources[ datasourceName ] = mysql;
		this.dataSource = datasourceName;
		this.sessionStorage = datasourceName;
	} else {
		// RAM cache: explicitly set timeToLiveSeconds so the entry's "until"
		// absolute lifetime is checked independently of read-driven idle resets
		// — matches Memcached/Redis put-TTL semantics where reads don't refresh.
		this.cache.connections[ "ldev6331cache" ] = {
			class: "lucee.runtime.cache.ram.RamCache",
			storage: true,
			custom: {
				timeToLiveSeconds: javacast( "int", url.ttlSeconds ),
				timeToIdleSeconds: 0
			}
		};
		this.sessionStorage = "ldev6331cache";
	}

	function onApplicationStart() {
		if ( url.sessionStorage eq "datasource" ) {
			try {
				query {
					echo( "DROP TABLE IF EXISTS cf_session_data" );
				}
			}
			catch ( any e ) { /* ignore */ }
		}
	}
}
