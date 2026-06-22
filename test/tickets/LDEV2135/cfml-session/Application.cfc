component {
	param name="url.sessionCluster";
	param name="url.SessionStorage";
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessionTimeout="#createTimeSpan(0,0,0,20)#";
	this.setClientCookies="yes";
	this.applicationTimeout="#createTimeSpan(0,0,0,20)#";
	this.name="ldev-2135-thread-session-cfml-#url.sessionCluster#-#url.SessionStorage#";
	this.sessionType="cfml";
	this.sessionCluster = url.sessionCluster;

	if (url.sessionStorage eq "redis" ){
		variables.redis = server.getTestService( "redis" );
		this.cache.connections[ "RedisSession" ] = {
			class: 'lucee.extension.io.cache.redis.RedisCache'
			, bundleName: 'redis.extension'
			, storage: true
			, custom: {
				"host": redis.server,
				"port": redis.port,
			}
		};
		this.sessionStorage="RedisSession";
	} else if (url.sessionStorage eq "memcached" ){
		variables.memcached = server.getDatasource("memcached");
		this.cache.connections[ "memcachedSession" ] = {
			class: 'org.lucee.extension.cache.mc.MemcachedCache'
			, bundleName: 'org.lucee.memcached.extension'
			, bundleVersion: server.getDefaultBundleVersion( 'org.lucee.memcached.extension', '4.0.0.14' )
			, storage: true
			, custom: {
				"socket_timeout":"3",
				"initial_connections":"1",
				"alive_check":"true",
				"buffer_size":"1",
				"max_spare_connections":"32",
				"storage_format":"Binary",
				"socket_connect_to":"3",
				"min_spare_connections":"1",
				"maint_thread_sleep":"5",
				"failback":"true",
				"max_idle_time":"600",
				"max_busy_time":"30",
				"nagle_alg":"true",
				"failover":"false",
				"servers":"#memcached.server#:#memcached.port#"
			}
			, default: ''
		};
		this.sessionStorage="memcachedSession";
	} else {
		this.sessionStorage="memory";
	}
}