component {
	param name="url.sessionStorage" default="ram";
	param name="url.sessionCluster" default=false;

	this.name = "ldev-6331-#url.sessionStorage#-cluster#url.sessionCluster#-" & hash( getCurrentTemplatePath() );
	this.sessionManagement = true;
	this.setClientCookies = true;
	this.sessionType = "application";
	this.sessionCluster = url.sessionCluster;
	// 2s timeout matches the cache TTL. After sleep > 2s,
	// purgeExpiredSessions (force=true) detects scope.isExpired()=true and
	// evicts the in-memory copy.
	this.sessionTimeout = createTimespan( 0, 0, 0, 2 );
	this.applicationTimeout = createTimespan( 0, 1, 0, 0 );

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
	} else {
		// RAM cache: explicitly set timeToLiveSeconds=2 so the entry's "until"
		// absolute lifetime is checked independently of read-driven idle resets
		// — matches Memcached/Redis put-TTL semantics where reads don't refresh.
		this.cache.connections[ "ldev6331cache" ] = {
			class: "lucee.runtime.cache.ram.RamCache",
			storage: true,
			custom: {
				timeToLiveSeconds: 2,
				timeToIdleSeconds: 0
			}
		};
	}

	this.sessionStorage = "ldev6331cache";
}
