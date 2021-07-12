component extends="org.lucee.cfml.test.LuceeTestCase" labels="memcached"{

	variables.cacheName='memcached';

	public function setUp(){
		variables.has=defineCache();
	}

	public boolean function isNotSupported() {
		if(isNull(variables.has)) setUp();
		return !variables.has;
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1210", function() {
			it( title='Checking to cache string using Memcached extension', skip=isNotSupported(), body=function( currentSpec ) {
				var testString = 'This is a test string';
				cachePut(id:'testStr', value:testString, cacheName:variables.cacheName);
				var cachedString = cacheget(id:'testStr', cacheName:variables.cacheName);
				var result = "";

				try{
					result=cachedString;
				}catch(any e){
					result=e.message;
				}

				expect(result).toBe("This is a test string");
			});

			it( title='Checking to cache query using Memcached extension', skip=isNotSupported(), body=function( currentSpec ) {
				var testQuery = queryNew("name,age","varchar,numeric",{name:["user1","user2"],age:[15,20]});
				cachePut(id:'testQry', value:testQuery, cacheName:variables.cacheName);	
				var cachedQuery = cacheget(id:'testQry', cacheName:variables.cacheName);
				var result = "";
				var result2 = "";

				try{
					result2=serialize(cachedQuery);
					result=cachedQuery.name[1];
				}catch(any e){
					result=e.message;
				}

				expect(result2).toBe("excpet an error here");
				expect(result).toBe("user1");
			});
		});
	}

	private boolean function defineCache(){
		try {
			application action="update"
				caches="#{memcached:{
						  class: 'org.lucee.extension.io.cache.memcache.MemCacheRaw'
						, bundleName: 'memcached.extension'
						, bundleVersion: '3.0.2.29'
						, storage: false
						, custom: {
							'socket_timeout':'30',
							'initial_connections':'1',
							'alive_check':'true',
							'buffer_size':'1',
							'max_spare_connections':'32',
							'storage_format':'Binary',
							'socket_connect_to':'3',
							'min_spare_connections':'1',
							'maint_thread_sleep':'5',
							'failback':'true',
							'max_idle_time':'600',
							'max_busy_time':'30',
							'nagle_alg':'true',
							'failover':'true',
							'servers':'localhost:11211'
						}
						, default: ''
					}}#";
			cachePut(id='abcd', value=1234, cacheName='memcached');
			return !isNull(cacheget(id:'abcd', cacheName:'memcached'));
		}
		catch(e) {}
		return false;
	}
}