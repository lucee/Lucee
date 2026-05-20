component {
	param name="url.sessionStorage" default="ram";
	param name="url.sessionCluster" default=false;
	param name="url.sessionKeepAlive" default="";

	this.name = "ldev-6331-#url.sessionStorage#-cluster#url.sessionCluster#-" & hash( getCurrentTemplatePath() );
	this.sessionManagement = true;
	this.setClientCookies = true;
	this.sessionType = "application";
	this.sessionCluster = url.sessionCluster;
	// 4s session timeout. Default sessionKeepAlive = sessionTimeout/2 = 2s — mid-TTL GET at 2.5s past keepAlive triggers a refresh.
	this.sessionTimeout = createTimespan( 0, 0, 0, 4 );
	this.applicationTimeout = createTimespan( 0, 1, 0, 0 );
	if ( len( url.sessionKeepAlive ) )
		this.sessionKeepAlive = createTimespan( 0, 0, 0, javacast( "int", url.sessionKeepAlive ) );

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
		// RAM cache: explicitly set timeToLiveSeconds=4 so the entry's "until"
		// absolute lifetime is checked independently of read-driven idle resets
		// — matches Memcached/Redis put-TTL semantics where reads don't refresh.
		this.cache.connections[ "ldev6331cache" ] = {
			class: "lucee.runtime.cache.ram.RamCache",
			storage: true,
			custom: {
				timeToLiveSeconds: 4,
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
