component extends = "org.lucee.cfml.test.LuceeTestCase" labels="redis" {

	function run( testResults, testBox ){
		describe( "Test case for LDEV4342", function(){
			
			it(title = "check admin cache connections are created",
					skip=isNotSupported(),
					body = function( currentSpec ){

				var redis = server.getTestService( "redis" );
					var cacheName = "LDEV-4342-check-redis-connection";
				admin
						action="getCacheConnections"
						type="server"
						password=server.SERVERADMINPASSWORD
						returnVariable="local.connectionsBefore";
				admin
					action="updateCacheConnection"
					type="server"
					password=server.SERVERADMINPASSWORD
					class="lucee.extension.io.cache.redis.simple.RedisCache"
					bundleName="redis.extension"
					name="#cacheName#"
					custom={
						"minIdle":8,
						"maxTotal":40,
						"maxIdle":24,
						"host":redis.server,
						"port":redis.port,
						"socketTimeout":2000,
						"liveTimeout":3600000,
						"idleTimeout":60000,
						"timeToLiveSeconds":0,
						"testOnBorrow":true,
						"rnd":1
					},
					default=""
					readonly=false
					storage=false
					remoteClients="";
				
				// does it work
				expect( cacheGetAllIds( filter="", cacheName=cacheName ) ).toBeArray("does cache exist?");

				admin
					action="getCacheConnections"
					type="server"
					password=server.SERVERADMINPASSWORD
					returnVariable="local.connectionsAfter";

				// set as a default cache for object
				admin
					action="updateCacheDefaultConnection"
					type="server"
					password=server.SERVERADMINPASSWORD
					object=cacheName
					template=""
					query=""
					resource=""
					function=""
					include=""
					http=""
					file=""
					webservice="";

				admin
					action="getCacheConnections"
					type="server"
					password=server.SERVERADMINPASSWORD
					returnVariable="local.connectionsDefault";
				
				// does it work
				expect( cacheGetAllIds( filter="", cacheName=cacheName ) ).toBeArray("does cache exist?");
				
				// does it get listed  LDEV-4533
				expect( queryColumnData( local.connectionsDefault, "default" ) ).toInclude( "object" );

				expect( queryColumnData( local.connectionsAfter, "name" ) ).toInclude( cacheName );
				expect( local.connectionsAfter.recordcount )
					.toBe( local.connectionsBefore.recordcount+1, "active cache connections" );
				
			});
		});
	}

	private boolean function isNotSupported() {
		var redis = server.getTestService( "redis" );
		return isNull(redis) || len(redis)==0;
	}
}