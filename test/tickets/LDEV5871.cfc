component extends="org.lucee.cfml.test.LuceeTestCase" labels="query,cache" {

	variables.cacheName = "testQueryCache_LDEV5871";
	variables.cacheProviders = [];
	variables.defaultCustom = {
		timeToLiveSeconds: 86400,
		timeToIdleSeconds: 86400
	};
	doDynamicSuiteConfig();

	function doDynamicSuiteConfig() {
		// Set up RAM cache (always available)
		setupCacheProvider("RAM", "lucee.runtime.cache.ram.RamCache");

		// Try to set up Redis if available
		var redis = server.getTestService("redis");
		if (!structIsEmpty(redis)) {
			setupCacheProvider("Redis", "org.lucee.extension.cache.redis.RedisCache", {
				"host": redis.SERVER,
				"port": redis.PORT
			});
		}

		// Try to set up Memcached if available
		var memcached = server.getTestService("memcached");
		if (!structIsEmpty(memcached)) {
			setupCacheProvider("Memcached", "org.lucee.extension.cache.mc.MemcachedCache", {
				"servers": "#memcached.SERVER#:#memcached.PORT#",
				"socket_timeout": "3",
				"socket_connect_to": "3"
			});
		}

		// EhCache setup (usually available as part of Lucee)
		try {
			setupCacheProvider("EhCache", "org.lucee.extension.cache.eh.EHCache");
		} catch (any e) {
			// EhCache not available
		}
	}

	function afterAll() {
		// Clean up all test caches
		for (var provider in variables.cacheProviders) {
			try {
				admin
					action="removeCacheConnection"
					type="server"
					password=server.SERVERADMINPASSWORD
					name=provider.name;
			} catch (any e) {
				// Ignore cleanup errors
			}
		}
	}

	function run(testResults, testBox) {

			// Run tests for each available cache provider
			for (var provider in variables.cacheProviders) {

				describe("Testing #provider.type# cache provider", function() {

					var currentProvider = provider;

					beforeEach(function() {
						// Set this provider as the default query cache
						admin
							action="updateCacheDefaultConnection"
							type="server"
							password=server.SERVERADMINPASSWORD
							query=currentProvider.name
							object=""
							template=""
							resource=""
							function=""
							include=""
							http=""
							file=""
							webservice="";
					});

					describe("basic cacheprefix functionality #currentProvider.name#", function() {

						it("should cache queries with same SQL and cacheprefix", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							query datasource="#dsn#" name="local.result1" cacheprefix="test-basic-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'test' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cacheprefix="test-basic-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'test' as col, NOW() as executed");
							}

							expect(result1.executed).toBe(result2.executed);
						});

						it("should work with queryExecute", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							var result1 = queryExecute(
								"SELECT 'test' as col, NOW() as executed",
								{},
								{
									datasource: dsn,
									cacheprefix: "test-queryexecute-",
									cachedwithin: createTimeSpan(0, 0, 1, 0)
								}
							);

							sleep(1000);

							var result2 = queryExecute(
								"SELECT 'test' as col, NOW() as executed",
								{},
								{
									datasource: dsn,
									cacheprefix: "test-queryexecute-",
									cachedwithin: createTimeSpan(0, 0, 1, 0)
								}
							);

							expect(result1.executed).toBe(result2.executed);
						});

					});

					describe("cacheprefix fallback behavior", function() {

						it("should fall back to auto-generated key when cacheprefix is empty", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							query datasource="#dsn#" name="local.result1" cacheprefix="" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'empty-test' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'empty-test' as col, NOW() as executed");
							}

							expect(result1.executed).toBe(result2.executed);
						});

						it("should fall back to auto-generated key when cacheprefix is whitespace", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							query datasource="#dsn#" name="local.result1" cacheprefix="   " cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'whitespace-test' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'whitespace-test' as col, NOW() as executed");
							}

							expect(result1.executed).toBe(result2.executed);
						});

					});

					describe("cacheprefix with other caching attributes", function() {

						it("should work with cachedwithin", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							query datasource="#dsn#" name="local.result1" cacheprefix="test-cachedwithin-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'combined' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cacheprefix="test-cachedwithin-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'combined' as col, NOW() as executed");
							}

							expect(result1.executed).toBe(result2.executed);
						});

						it("should work with cachedafter", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));
							var cacheAfterDate = dateAdd("s", -10, now());

							query datasource="#dsn#" name="local.result1" cacheprefix="test-cachedafter-" cachedwithin="#createTimeSpan(0,0,1,0)#" cachedafter="#cacheAfterDate#" {
								echo("SELECT 'cachedafter-test' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cacheprefix="test-cachedafter-" cachedwithin="#createTimeSpan(0,0,1,0)#" cachedafter="#cacheAfterDate#" {
								echo("SELECT 'cachedafter-test' as col, NOW() as executed");
							}

							expect(result1.executed).toBe(result2.executed);
						});

						it("should not cache when cacheprefix is used without cachedwithin or cachedafter", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							query datasource="#dsn#" name="local.result1" cacheprefix="ignored-prefix-" {
								echo("SELECT 'not-cached' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cacheprefix="ignored-prefix-" {
								echo("SELECT 'not-cached' as col, NOW() as executed");
							}

							expect(result1.executed).notToBe(result2.executed);
						});

					});

					describe("cacheprefix isolation", function() {

						it("should not share cache when different SQL with same cacheprefix", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							query datasource="#dsn#" name="local.result1" cacheprefix="shared-prefix-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'first' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cacheprefix="shared-prefix-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'second' as col, NOW() as executed");
							}

							expect(result1.executed).notToBe(result2.executed);
							expect(result1.col).toBe("first");
							expect(result2.col).toBe("second");
						});

						it("should not share cache when same SQL with different cacheprefix", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							query datasource="#dsn#" name="local.result1" cacheprefix="prefix-a-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'same-sql' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cacheprefix="prefix-b-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'same-sql' as col, NOW() as executed");
							}

							expect(result1.executed).notToBe(result2.executed);
						});

						it("should support multiple concurrent cacheprefix values", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							query datasource="#dsn#" name="local.result1" cacheprefix="concurrent-1-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 1 as num, NOW() as executed");
							}

							query datasource="#dsn#" name="local.result2" cacheprefix="concurrent-2-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 2 as num, NOW() as executed");
							}

							query datasource="#dsn#" name="local.result3" cacheprefix="concurrent-3-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 3 as num, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result1b" cacheprefix="concurrent-1-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 1 as num, NOW() as executed");
							}

							query datasource="#dsn#" name="local.result2b" cacheprefix="concurrent-2-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 2 as num, NOW() as executed");
							}

							query datasource="#dsn#" name="local.result3b" cacheprefix="concurrent-3-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 3 as num, NOW() as executed");
							}

							expect(result1.executed).toBe(result1b.executed);
							expect(result2.executed).toBe(result2b.executed);
							expect(result3.executed).toBe(result3b.executed);
						});

					});

					describe("cacheprefix edge cases", function() {

						it("should handle long cacheprefix values", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));
							var longPrefix = repeatString("a", 200) & "-";

							query datasource="#dsn#" name="local.result1" cacheprefix="#longPrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'long-prefix' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cacheprefix="#longPrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'long-prefix' as col, NOW() as executed");
							}

							expect(result1.executed).toBe(result2.executed);
						});

						it("should handle special characters in cacheprefix", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));
							var specialPrefix = "test-prefix_with.special@chars##123-";

							query datasource="#dsn#" name="local.result1" cacheprefix="#specialPrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'special-chars' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cacheprefix="#specialPrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'special-chars' as col, NOW() as executed");
							}

							expect(result1.executed).toBe(result2.executed);
						});

						it("should handle unicode characters in cacheprefix", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));
							var unicodePrefix = "test-καλημέρα-世界-";

							query datasource="#dsn#" name="local.result1" cacheprefix="#unicodePrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'unicode' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cacheprefix="#unicodePrefix#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'unicode' as col, NOW() as executed");
							}

							expect(result1.executed).toBe(result2.executed);
						});

						it("should be case-sensitive", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							query datasource="#dsn#" name="local.result1" cacheprefix="myprefix-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'case-test' as col, NOW() as executed");
							}

							sleep(1000);

							query datasource="#dsn#" name="local.result2" cacheprefix="MYPREFIX-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'case-test' as col, NOW() as executed");
							}

							expect(result1.executed).notToBe(result2.executed);
						});

					});

					describe("cacheprefix with query parameters", function() {

						it("should cache queries with query parameters", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							var result1 = queryExecute(
								"SELECT ? as col, NOW() as executed",
								["test-value"],
								{
									datasource: dsn,
									cacheprefix: "with-params-",
									cachedwithin: createTimeSpan(0, 0, 1, 0)
								}
							);

							sleep(1000);

							var result2 = queryExecute(
								"SELECT ? as col, NOW() as executed",
								["test-value"],
								{
									datasource: dsn,
									cacheprefix: "with-params-",
									cachedwithin: createTimeSpan(0, 0, 1, 0)
								}
							);

							expect(result1.executed).toBe(result2.executed);
						});

					});

					describe("cacheRemoveAll() with cacheprefix", function() {

						it("should remove cached query using cacheRemove #currentProvider.name#", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));
							var cacheKey = "test-remove-" & createUUID();

							// Cache a query
							query datasource="#dsn#" name="local.result1" cacheprefix="#cacheKey#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'remove-test' as col, NOW() as executed");
							}

							sleep(1000);

							// Remove from cache
							cacheRemoveAll(currentProvider.name);

							// Query again - should not be cached
							query datasource="#dsn#" name="local.result2" cacheprefix="#cacheKey#" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'remove-test' as col, NOW() as executed");
							}

							expect(result1.executed).notToBe(result2.executed);
						});

						it("should remove cached query without cacheprefix using cacheRemoveAll", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							// Cache a query without cacheprefix
							var sql = "SELECT 'remove-no-prefix' as col, NOW() as executed";
							query datasource="#dsn#" name="local.result1" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo(sql);
							}

							sleep(1000);

							// Remove from cache
							cacheRemoveAll(currentProvider.name);

							// Query again - should not be cached
							query datasource="#dsn#" name="local.result2" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo(sql);
							}

							expect(result1.executed).notToBe(result2.executed);
						});

					});

					describe("cacheClear() with query caches", function() {

						it("should clear all cached queries using cacheClear", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							// Cache multiple queries
							query datasource="#dsn#" name="local.result1" cacheprefix="clear-test-1-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 1 as num, NOW() as executed");
							}

							query datasource="#dsn#" name="local.result2" cacheprefix="clear-test-2-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 2 as num, NOW() as executed");
							}

							query datasource="#dsn#" name="local.result3" cacheprefix="clear-test-3-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 3 as num, NOW() as executed");
							}

							sleep(1000);

							// Clear the entire cache
							cacheClear("clear-test-*", currentProvider.name);

							// Query again - none should be cached
							query datasource="#dsn#" name="local.result1b" cacheprefix="clear-test-1-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 1 as num, NOW() as executed");
							}

							query datasource="#dsn#" name="local.result2b" cacheprefix="clear-test-2-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 2 as num, NOW() as executed");
							}

							query datasource="#dsn#" name="local.result3b" cacheprefix="clear-test-3-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 3 as num, NOW() as executed");
							}

							expect(result1.executed).notToBe(result1b.executed);
							expect(result2.executed).notToBe(result2b.executed);
							expect(result3.executed).notToBe(result3b.executed);
						});

						it("should clear queries with and without cacheprefix", function() {
							var dsn = server.getDatasource("h2", server._getTempDir("LDEV5871"));

							// Cache with prefix
							query datasource="#dsn#" name="local.result1" cacheprefix="mixed-test-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'with-prefix' as col, NOW() as executed");
							}

							// Cache without prefix
							query datasource="#dsn#" name="local.result2" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'without-prefix' as col, NOW() as executed");
							}

							sleep(1000);

							// Clear the entire cache
							cacheClear("*", currentProvider.name);

							// Query again - neither should be cached
							query datasource="#dsn#" name="local.result1b" cacheprefix="mixed-test-" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'with-prefix' as col, NOW() as executed");
							}

							query datasource="#dsn#" name="local.result2b" cachedwithin="#createTimeSpan(0,0,1,0)#" {
								echo("SELECT 'without-prefix' as col, NOW() as executed");
							}

							expect(result1.executed).notToBe(result1b.executed);
							expect(result2.executed).notToBe(result2b.executed);
						});

					});

				});
			}
	}

	private function setupCacheProvider(required string type, required string cacheClass, struct custom={}) {
		var cacheName = variables.cacheName & "_" & arguments.type;

		try {
			structAppend(defaultCustom, arguments.custom);

			admin
				action="updateCacheConnection"
				type="server"
				password=server.SERVERADMINPASSWORD
				name=cacheName
				class=arguments.cacheClass
				storage=false
				default=""
				custom=variables.defaultCustom;

			arrayAppend(variables.cacheProviders, {
				type: arguments.type,
				name: cacheName,
				class: arguments.cacheClass
			});

		} catch (any e) {
			// Provider setup failed, skip it
		}
	}

}
