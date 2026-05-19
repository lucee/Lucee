<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="memcached" {

	variables.cacheName = 'ldev6324mc';

	public function setUp(){
		variables.has = defineCache();
	}

	public boolean function isNotSupported() {
		if ( isNull( variables.has ) ) setUp();
		return !variables.has;
	}

	public void function testCacheCountReturnsMinusOneWhenEnumerationUnsupported() skip="isNotSupported" {
		// memcached's keys() returns null because the protocol can't enumerate.
		// cacheCount() must return -1 (the documented "not available" sentinel)
		// instead of NPEing on .size()
		cachePut( id="ldev6324", value="x", cacheName=variables.cacheName );
		var count = cacheCount( cacheName=variables.cacheName );
		assertEquals( -1, count );
	}

	private boolean function defineCache(){
		var memcached = server.getDatasource( "memcached" );
		if ( isEmpty( memcached ) )
			return false;

		application action="update"
			caches="#{ '#variables.cacheName#': {
				  class: 'org.lucee.extension.cache.mc.MemcachedCache'
				, bundleName: 'org.lucee.memcached.extension'
				, bundleVersion: server.getDefaultBundleVersion( 'org.lucee.memcached.extension', '4.0.0.14' )
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
			} }#";
		cachePut( id="abcd", value=1234, cacheName=variables.cacheName );
		return !isNull( cacheGet( id="abcd", cacheName=variables.cacheName ) );
	}

}
</cfscript>
