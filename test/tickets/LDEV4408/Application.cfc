component {
	variables.redis = server.getTestService( "redis" );

	this.cache.connections["RedisSession"] = {
		class: 'lucee.extension.io.cache.redis.simple.RedisCache'
		, bundleName: 'redis.extension'
		, storage: true
		, custom: {
			"host": redis.server,
			"port": redis.port,
		}
	};

	this.name='LDEV-4408';
	this.sessionManagement=true;
	this.setClientCookies=true;
	this.sessionStorage="RedisSession";
	this.sessionCluster=true;

	public void function onSessionStart() {
		session.trackingId = createGUID();
	}

}