component extends="org.lucee.cfml.test.LuceeTestCase" labels="ram" {

	function afterAll() {
		if ( directoryExists( "ram://LDEV1109" ) ) {
			directoryDelete( "ram://LDEV1109", true );
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1109 RAM drive should survive cache eviction", function() {

			it( title="ram:// root should exist after cache is cleared", body=function() {
				// verify root exists before clear
				expect( directoryExists( "ram://" ) ).toBeTrue();

				// clear the cache, simulating GC reclaiming all references
				var ramCache = _getRamCache();
				ramCache.clear();

				// root should be auto-recreated on next access
				expect( directoryExists( "ram://" ) ).toBeTrue();
			});

			it( title="ram:// directories should be stored with HardRef not SoftRef", body=function() {
				var dir = "ram://LDEV1109";
				// cleanup from previous run
				if ( directoryExists( dir ) ) directoryDelete( dir, true );

				// create directory structure with a file
				directoryCreate( dir );
				directoryCreate( dir & "/subdir" );
				fileWrite( dir & "/subdir/file.txt", "data" );

				// use reflection to access the private entries map in RamCache
				var ramCache = _getRamCache();
				var entriesField = ramCache.getClass().getDeclaredField( "entries" );
				entriesField.setAccessible( true );
				var entries = entriesField.get( ramCache );

				var HardRef = createObject( "java", "lucee.runtime.cache.ram.ref.HardRef" );
				var dirCount = 0;
				var fileCount = 0;

				var iter = entries.values().iterator();
				while ( iter.hasNext() ) {
					var ref = iter.next();
					var rce = ref.get();
					if ( isNull( rce ) ) continue;
					var val = rce.getValue();
					if ( !isInstanceOf( val, "lucee.commons.io.res.type.cache.CacheResourceCore" ) ) continue;

					if ( val.getType() == 1 ) {
						// TYPE_DIRECTORY = 1: must be pinned with HardRef
						expect( ref ).toBeInstanceOf( "lucee.runtime.cache.ram.ref.HardRef",
							"directory entry should use HardRef, not SoftRef" );
						dirCount++;
					}
					else if ( val.getType() == 2 ) {
						// TYPE_FILE = 2: should use SoftRef (reclaimable under memory pressure)
						expect( ref ).toBeInstanceOf( "lucee.runtime.cache.ram.ref.SoftRef",
							"file entry should use SoftRef, not HardRef" );
						fileCount++;
					}
				}

				// root + LDEV1109 + subdir = at least 3 directories
				expect( dirCount ).toBeGTE( 3, "should have at least 3 directory entries" );
				// at least 1 file
				expect( fileCount ).toBeGTE( 1, "should have at least 1 file entry" );
			});
		});
	}

	private function _getRamCache() {
		var providers = getPageContext().getConfig().getResourceProviders();
		for ( var p in providers ) {
			if ( p.getScheme() == "ram" ) {
				return p.getCache();
			}
		}
		throw( message="RAM resource provider not found" );
	}
}
