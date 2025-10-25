component extends="org.lucee.cfml.test.LuceeTestCase" labels="query,cache" {

	variables.cacheName = "testQueryCache_AD0000";

	function beforeAll() {
		// Create a test RAM cache for query caching
		admin
			action="updateCacheConnection"
			type="web"
			password="#request.webadminpassword#"
			name="#variables.cacheName#"
			class="lucee.runtime.cache.ram.RamCache"
			storage="false"
			default="query"
			custom="#{
				timeToLiveSeconds: 86400,
				timeToIdleSeconds: 86400
			}#";
	}

	function afterAll() {
		// Clean up test cache
		try {
			admin
				action="removeCacheConnection"
				type="web"
				password="#request.webadminpassword#"
				name="#variables.cacheName#";
		} catch (any e) {
			// Ignore cleanup errors
		}
	}

	function testBasicCacheprefixFunctionality() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		// First execution - should cache with custom prefix
		query datasource="#dsn#" name="local.result1" cacheprefix="test-basic-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'test' as col, NOW() as executed");
		}

		sleep(1000);; // Small delay to ensure different timestamp if not cached

		// Second execution with same SQL and cacheprefix - should retrieve from cache (same timestamp)
		query datasource="#dsn#" name="local.result2" cacheprefix="test-basic-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'test' as col, NOW() as executed");
		}

		// Verify both have same execution time (proving cache hit)
		assertEquals(result1.executed, result2.executed, "Query with same SQL and cacheprefix should be retrieved from cache");
	}

	function testQueryExecuteWithCacheprefix() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		var result1 = queryExecute(
			"SELECT 'test' as col, NOW() as executed",
			{},
			{
				datasource: dsn,
				cacheprefix: "test-queryexecute-",
				cachedwithin: createTimeSpan(0, 0, 1, 0)
			}
		);

		sleep(1000);;

		var result2 = queryExecute(
			"SELECT 'test' as col, NOW() as executed",
			{},
			{
				datasource: dsn,
				cacheprefix: "test-queryexecute-",
				cachedwithin: createTimeSpan(0, 0, 1, 0)
			}
		);

		assertEquals(result1.executed, result2.executed, "QueryExecute should use cache with same SQL and cacheprefix");
	}

	function testEmptyCacheprefixFallback() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		// Empty string should fall back to auto-generated key (no prefix)
		query datasource="#dsn#" name="local.result1" cacheprefix="" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'empty-test' as col, NOW() as executed");
		}

		sleep(1000);;

		// Same query without cacheprefix should hit same auto-generated cache
		query datasource="#dsn#" name="local.result2" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'empty-test' as col, NOW() as executed");
		}

		assertEquals(result1.executed, result2.executed, "Empty cacheprefix should fall back to auto-generated key");
	}

	function testWhitespaceCacheprefixFallback() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		// Whitespace-only should fall back to auto-generated key (no prefix)
		query datasource="#dsn#" name="local.result1" cacheprefix="   " cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'whitespace-test' as col, NOW() as executed");
		}

		sleep(1000);;

		// Same query without cacheprefix should hit same auto-generated cache
		query datasource="#dsn#" name="local.result2" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'whitespace-test' as col, NOW() as executed");
		}

		assertEquals(result1.executed, result2.executed, "Whitespace cacheprefix should fall back to auto-generated key");
	}

	function testCacheprefixWithCachedwithin() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		// Both cacheprefix and cachedwithin should work together
		query datasource="#dsn#" name="local.result1" cacheprefix="test-with-cachedwithin-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'combined' as col, NOW() as executed");
		}

		sleep(1000);;

		// Within TTL - should hit cache
		query datasource="#dsn#" name="local.result2" cacheprefix="test-with-cachedwithin-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'combined' as col, NOW() as executed");
		}

		// Should have same execution time (proving cache hit)
		assertEquals(result1.executed, result2.executed, "Cache should be hit within TTL");
	}

	function testCacheprefixWithCachedafter() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));
		var cacheAfterDate = dateAdd("s", -10, now());

		// Query with cachedafter
		query datasource="#dsn#" name="local.result1" cacheprefix="test-with-cachedafter-" cachedwithin="#createTimeSpan(0,0,1,0)#" cachedafter="#cacheAfterDate#" {
			echo("SELECT 'cachedafter-test' as col, NOW() as executed");
		}

		sleep(1000);;

		// Should hit cache
		query datasource="#dsn#" name="local.result2" cacheprefix="test-with-cachedafter-" cachedwithin="#createTimeSpan(0,0,1,0)#" cachedafter="#cacheAfterDate#" {
			echo("SELECT 'cachedafter-test' as col, NOW() as executed");
		}

		assertEquals(result1.executed, result2.executed, "Cache should be hit with cachedafter");
	}

	function testDifferentSQLWithSameCacheprefix() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		// First query with cacheprefix prefix
		query datasource="#dsn#" name="local.result1" cacheprefix="shared-prefix-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'first' as col, NOW() as executed");
		}

		sleep(1000);;

		// Different SQL with same cacheprefix prefix - should NOT share cache
		// (because auto-generated part based on SQL is different)
		query datasource="#dsn#" name="local.result2" cacheprefix="shared-prefix-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'second' as col, NOW() as executed");
		}

		// Different SQL = different auto-generated ID = different cache entries
		assertNotEquals(result1.executed, result2.executed, "Different SQL with same cacheprefix should NOT share cache");
		assertEquals("first", result1.col, "First query should have its own data");
		assertEquals("second", result2.col, "Second query should have its own data");
	}

	function testDifferentCacheprefixSameSQL() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		// Execute with first prefix
		query datasource="#dsn#" name="local.result1" cacheprefix="prefix-a-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'same-sql' as col, NOW() as executed");
		}

		sleep(1000);;

		// Same SQL, different cacheprefix prefix - should NOT hit cache
		query datasource="#dsn#" name="local.result2" cacheprefix="prefix-b-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'same-sql' as col, NOW() as executed");
		}

		// Different prefixes = different full cache IDs = different timestamps
		assertNotEquals(result1.executed, result2.executed, "Different cacheprefix prefixes should create separate cache entries");
	}

	function testCacheprefixWithoutCachingAttributes() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		// cacheprefix without cachedwithin/cachedafter should be ignored (no caching occurs)
		query datasource="#dsn#" name="local.result1" cacheprefix="ignored-prefix-" {
			echo("SELECT 'not-cached' as col, NOW() as executed");
		}

		sleep(1000);;

		// Second execution - should NOT hit cache (caching was not enabled)
		query datasource="#dsn#" name="local.result2" cacheprefix="ignored-prefix-" {
			echo("SELECT 'not-cached' as col, NOW() as executed");
		}

		// Should have different timestamps (no caching occurred)
		assertNotEquals(result1.executed, result2.executed, "Queries without cachedwithin/cachedafter should not be cached");
	}

	function testCacheprefixEdgeCases() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		// Test 1: Long cache path prefix
		var longPrefix = repeatString("a", 200) & "-";
		query datasource="#dsn#" name="local.result1" cacheprefix="#longPrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'long-prefix' as col, NOW() as executed");
		}

		sleep(1000);;

		// Should hit cache with long prefix
		query datasource="#dsn#" name="local.result1b" cacheprefix="#longPrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'long-prefix' as col, NOW() as executed");
		}
		assertEquals(result1.executed, result1b.executed, "Long cache path prefix should work");

		// Test 2: Special characters in prefix
		var specialPrefix = "test-prefix_with.special@chars##123-";
		query datasource="#dsn#" name="local.result2" cacheprefix="#specialPrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'special-chars' as col, NOW() as executed");
		}

		sleep(1000);;

		query datasource="#dsn#" name="local.result2b" cacheprefix="#specialPrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'special-chars' as col, NOW() as executed");
		}
		assertEquals(result2.executed, result2b.executed, "Cache path with special characters should work");

		// Test 3: Unicode characters in prefix
		var unicodePrefix = "test-prefix-καλημέρα-世界-";
		query datasource="#dsn#" name="local.result3" cacheprefix="#unicodePrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'unicode' as col, NOW() as executed");
		}

		sleep(1000);;

		query datasource="#dsn#" name="local.result3b" cacheprefix="#unicodePrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'unicode' as col, NOW() as executed");
		}
		assertEquals(result3.executed, result3b.executed, "Cache path with unicode should work");
	}

	function testCacheprefixCaseSensitivity() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		// Cache with lowercase prefix
		query datasource="#dsn#" name="local.result1" cacheprefix="myprefix-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'case-test' as col, NOW() as executed");
		}

		sleep(1000);;

		// Try with uppercase prefix - should NOT hit cache (different prefix)
		query datasource="#dsn#" name="local.result2" cacheprefix="MYPREFIX-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 'case-test' as col, NOW() as executed");
		}

		// Different case = different prefixes = different cache entries
		assertNotEquals(result1.executed, result2.executed, "Cache path prefixes should be case-sensitive");
	}

	function testMultipleConcurrentCacheprefixs() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		// Create multiple cached queries with different prefixes
		query datasource="#dsn#" name="local.result1" cacheprefix="concurrent-1-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 1 as num, NOW() as executed");
		}

		query datasource="#dsn#" name="local.result2" cacheprefix="concurrent-2-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 2 as num, NOW() as executed");
		}

		query datasource="#dsn#" name="local.result3" cacheprefix="concurrent-3-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 3 as num, NOW() as executed");
		}

		sleep(1000);;

		// Verify each can be retrieved from cache independently
		query datasource="#dsn#" name="local.result1b" cacheprefix="concurrent-1-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 1 as num, NOW() as executed");
		}

		query datasource="#dsn#" name="local.result2b" cacheprefix="concurrent-2-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 2 as num, NOW() as executed");
		}

		query datasource="#dsn#" name="local.result3b" cacheprefix="concurrent-3-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
			echo("SELECT 3 as num, NOW() as executed");
		}

		// Verify cache hits occurred
		assertEquals(result1.executed, result1b.executed, "First cached query should be retrieved");
		assertEquals(result2.executed, result2b.executed, "Second cached query should be retrieved");
		assertEquals(result3.executed, result3b.executed, "Third cached query should be retrieved");
	}

	function testCacheprefixWithQueryParams() {
		var dsn = server.getDatasource("h2", server._getTempDir("LDEV-5871"));

		// Query with params and cacheprefix
		```
		<cfquery datasource="#dsn#" name="local.result1" cacheprefix="with-params-" cachedwithin="#createTimeSpan(0,0,1,0)#" >
			SELECT 
			<cfqueryparam value="test-value" cfsqltype="cf_sql_varchar"> as col, NOW() as executed
		</cfquery>
		```

		sleep(1000);;

		// Same query with same params and cacheprefix - should hit cache
		```
		<cfquery datasource="#dsn#" name="local.result2" cacheprefix="with-params-" cachedwithin="#createTimeSpan(0,0,1,0)#" >
			SELECT 
			<cfqueryparam value="test-value" cfsqltype="cf_sql_varchar"> as col, NOW() as executed
		</cfquery>
		```
		assertEquals(result1.executed, result2.executed, "Query with params should be cached");
	}

	private function repeatString(required string str, required numeric times) {
		var result = "";
		for (var i = 1; i <= times; i++) {
			result &= str;
		}
		return result;
	}
}
