component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, textbox ) {

		describe(title="LDEV-5758 test merging cfconfig.json - extensions", body=function(){

			it(title = "re-importing the same config should result in no changes", body = function ( currentSpec ){
				var merger = _getMerger();

				var current = deserializeJSON( FileRead( expandPath( "{lucee-config}/.CFConfig.json" ) ) );

				var src = duplicate( current );
				var import = duplicate( current );

				merger.merge( src, import );

				expect( src ).toHaveLength( structCount( current ) );

				var srcJson = serializeJSON( var=src, compact="false" );
				var currentJson = serializeJSON( var=current, compact="false" );
				
				expect( srcJson ).toBe( currentJson );
			});
		});

		describe(title="LDEV-5758 test merging cfconfig.json - extensions", body=function(){

			it(title = "check config import update", body = function ( currentSpec ){
				var merger = _getMerger();

				var src = {
					"extensions": [{
						"id": "7E673D15-D87C-41A6-8B5F1956528C605F",
						"name": "MySQL",
						"version": "9.2.0"
					}]
				};

				var import = {
					"extensions": [{
						"id": "7E673D15-D87C-41A6-8B5F1956528C605F",
						"name": "MySQL",
						"version": "9.3.0"
					}]
				};

				merger.merge( src, import );

				expect( src.extensions ).toHaveLength( 1 );
				expect( src.extensions[1].version ).toBe( "9.3.0" );
			});

			it(title = "check config import append", body = function ( currentSpec ){
				var merger = _getMerger();

				var src= {
					"extensions": [{
						"id": "7E673D15-D87C-41A6-8B5F1956528C605F",
						"name": "MySQL",
						"version": "9.3.0"
					}]
				};

				var import = {
					"extensions": [{
						"id": "671B01B8-B3B3-42B9-AC055A356BED5281",
						"name": "PostgreSQL",
						"version": "42.7.7"
					}]
				};

				merger.merge( src, import );

				expect( src.extensions ).toHaveLength( 2 );
				expect( src.extensions[1].version ).toBe( "9.3.0" );
				expect( src.extensions[2].version ).toBe( "42.7.7" );
			});
		

		});

		describe(title="LDEV-5758 test merging cfconfig.json - resource providers", body=function(){

			it(title = "check config import update", body = function ( currentSpec ){
				var merger = _getMerger();

				var src= {
					"resourceProviders": [{
						"scheme": "ftp",
						"class": "lucee.commons.io.res.type.ftp.FTPResourceProvider",
						"arguments": "lock-timeout:20000;socket-timeout:-1;client-timeout:60000"
					}]
				};

				var import = {
					"resourceProviders": [{
						"scheme": "ftp",
						"class": "lucee.commons.io.res.type.ftp.FTPResourceProvider2",
						"arguments": "lock-timeout:20000;socket-timeout:-1;client-timeout:60000"
					}]
				};

				merger.merge( src, import );

				expect( src.resourceProviders ).toHaveLength( 1 );
				expect( src.resourceProviders[1].class).toBe( "lucee.commons.io.res.type.ftp.FTPResourceProvider2" );
			});

			it(title = "check config import append", body = function ( currentSpec ){
				var merger = _getMerger();

				var src= {
					"resourceProviders": [{
						"scheme": "ftp",
						"class": "lucee.commons.io.res.type.ftp.FTPResourceProvider",
						"arguments": "lock-timeout:20000;socket-timeout:-1;client-timeout:60000"
					}]
				};

				var import = {
					"resourceProviders": [{
						"scheme": "zip",
						"class": "lucee.commons.io.res.type.zip.ZipResourceProvider",
						"arguments": "lock-timeout:1000;case-sensitive:true;"
					}]
				};

				merger.merge( src, import );

				expect( src.resourceProviders ).toHaveLength( 2 );
				expect( src.resourceProviders[1].scheme ).toBe( "ftp" );
				expect( src.resourceProviders[2].scheme ).toBe( "zip" );
			});
		

		});

		describe(title="LDEV-5758 test merging cfconfig.json - scheduled tasks", body=function(){

			it(title = "check config import update", body = function ( currentSpec ){
				var merger = _getMerger();

				var src= {
					"scheduledTasks": [{
						"name": "oneoff",
						"startDate": "{d '2025-07-30'}",
						"startTime": "{t '15:09:00'}",
						"url": "http://127.0.0.1:9888/task.cfm?scheduled=true",
						"port": 9888,
						"interval": "once",
						"timeout": 50000,
					}]
				};

				var import = {
					"scheduledTasks": [{
						"name": "oneoff",
						"startDate": "{d '2025-07-30'}",
						"startTime": "{t '15:09:00'}",
						"url": "http://127.0.0.1:9888/task.cfm?scheduled=true",
						"port": 8888,
						"interval": "once",
						"timeout": 50000,
					}]
				};

				merger.merge( src, import );

				expect( src.scheduledTasks ).toHaveLength( 1 );
				expect( src.scheduledTasks[1].port ).toBe( 8888 );
			});

			it(title = "check config import append", body = function ( currentSpec ){
				var merger = _getMerger();

				var src= {
					"scheduledTasks": [{
						"name": "oneoff",
						"startDate": "{d '2025-07-30'}",
						"startTime": "{t '15:09:00'}",
						"url": "http://127.0.0.1:9888/task.cfm?scheduled=true",
						"port": 8888,
						"interval": "once",
						"timeout": 50000,
					}]
				};

				var import = {
					"scheduledTasks": [{
						"name": "oneoff2",
						"startDate": "{d '2025-07-30'}",
						"startTime": "{t '15:09:00'}",
						"url": "http://127.0.0.1:9888/task.cfm?scheduled=true",
						"port": 8888,
						"interval": "once",
						"timeout": 50000,
					}]
				};

				merger.merge( src, import );

				expect( src.scheduledTasks ).toHaveLength( 2 );
				expect( src.scheduledTasks[1].name ).toBe( "oneoff" );
				expect( src.scheduledTasks[2].name ).toBe( "oneoff2" );
			});

		});

	describe(title="LDEV-5758 test merging cfconfig.json - dumpWriters", body=function(){

			it(title = "check config import update", body = function ( currentSpec ){
				var merger = _getMerger();

				var src= {
					"dumpWriters": [{
						"name": "html",
						"class": "lucee.runtime.dump.HTMLDumpWriter",
						"default": "browser"
					}]
				};

				var import = {
					"dumpWriters": [{
						"name": "html",
						"class": "lucee.runtime.dump.HTMLDumpWriter2",
						"default": "browser"
					}]

				};

				merger.merge( src, import );

				expect( src.dumpWriters ).toHaveLength( 1 );
				expect( src.dumpWriters[1].class ).toBe( "lucee.runtime.dump.HTMLDumpWriter2" );
			});

			it(title = "check config import append", body = function ( currentSpec ){
				var merger = _getMerger();

				var src= {
					"dumpWriters": [{
						"name": "html",
						"class": "lucee.runtime.dump.HTMLDumpWriter",
						"default": "browser"
					}]

				};

				var import = {
					"dumpWriters": [{
						"name": "text",
						"class": "lucee.runtime.dump.TextDumpWriter",
						"default": "console"
					}]
				};

				merger.merge( src, import );

				expect( src.dumpWriters ).toHaveLength( 2 );
				expect( src.dumpWriters[1].name ).toBe( "html" );
				expect( src.dumpWriters[2].name ).toBe( "text" );
			});
		

		});

		describe(title="LDEV-5758 test merging cfconfig.json - cacheClasess", body=function(){

			it(title = "check config import update", body = function ( currentSpec ){
				var merger = _getMerger();

				var src= {
					"cacheClasses": [{
						"class": "org.lucee.extension.cache.eh.EHCache",
						"bundleName": "ehcache.extension",
						"bundleVersion": "2.10.0.36"
					}]
				};

				var import = {
					"cacheClasses": [{
						"class": "org.lucee.extension.cache.eh.EHCache",
						"bundleName": "ehcache.extension",
						"bundleVersion": "2.10.0.37"
					}]
				};

				merger.merge( src, import );

				expect( src.cacheClasses ).toHaveLength( 1 );
				expect( src.cacheClasses[1].bundleVersion ).toBe( "2.10.0.37" );
			});

			it(title = "check config import append", body = function ( currentSpec ){
				var merger = _getMerger();

				var src= {
					"cacheClasses": [{
						"class": "org.lucee.extension.cache.eh.EHCache",
						"bundleName": "ehcache.extension",
						"bundleVersion": "2.10.0.36"
					}]
				};

				var import = {
					"cacheClasses": [{
						"class": "lucee.extension.io.cache.redis.simple.RedisCache",
						"bundleName": "redis.extension",
						"bundleVersion": "4.0.0.0-SNAPSHOT"
					}]
				};

				merger.merge( src, import );

				expect( src.cacheClasses ).toHaveLength(2);
				expect( src.cacheClasses[1].bundleVersion ).toBe( "2.10.0.36" );
				expect( src.cacheClasses[2].bundleVersion ).toBe( "4.0.0.0-SNAPSHOT" );
			});
		

		});

	}
	
	private function _getMerger(){
		var mergeComponent = new component {
			
			import "lucee.runtime.config.ConfigMerge";

			function merge(src, import){
				ConfigMerge::merge(src, import);
				return src;
			}
		};

		return mergeComponent;
	}
}