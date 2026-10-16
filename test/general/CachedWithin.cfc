<!--- 
 *
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 --->
<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache" {

	variables.queryCacheName = "testQueryCache";
	variables.functionCacheName = "testFunctionCache";
	variables.httpCacheName = "testHttpCache";
	variables.datasourceName = "testCacheDS";
	variables.httpbin = server.getTestService("httpbin");

	function beforeAll() {
		variables.hasDS = defineDatasource();
		variables.hasHttpbin = structCount(variables.httpbin) > 0;
		defineCaches();
	}

	function afterAll() {
		// Clean up caches
		try {
			cacheClear(variables.queryCacheName);
			cacheClear(variables.functionCacheName);
			cacheClear(variables.httpCacheName);
		} catch(any e) {
			// Ignore cleanup errors
		}
	}

	// ========================================
	// HTTP Tests
	// ========================================

	public void function testHTTPCachedWithinId() localmode=true {
		if (!variables.hasHttpbin) return;

		cfhttp(
			url="http://#variables.httpbin.server#:#variables.httpbin.port#/uuid",
			result="local.httpResult",
			cachedwithin="#createTimeSpan(0,0,10,0)#"
		);

		var cacheId = cachedWithinId(local.httpResult);

		expect(cacheId).toBeString();
		expect(cacheId).notToBeEmpty();
	}

	public void function testHTTPCachedWithinFlush() localmode=true {
		if (!variables.hasHttpbin) return;

		// First request - creates cache
		cfhttp(
			url="http://#variables.httpbin.server#:#variables.httpbin.port#/uuid",
			result="local.httpResult1",
			cachedwithin="#createTimeSpan(0,1,0,0)#"
		);
		var firstResponse = local.httpResult1.filecontent;

		// Second request - should return cached result (same UUID)
		cfhttp(
			url="http://#variables.httpbin.server#:#variables.httpbin.port#/uuid",
			result="local.httpResult2",
			cachedwithin="#createTimeSpan(0,1,0,0)#"
		);
		var secondResponse = local.httpResult2.filecontent;

		// Verify cache is working (same response)
		expect(firstResponse).toBe(secondResponse);

		// Flush the cache
		var flushResult = cachedWithinFlush(local.httpResult1);
		expect(flushResult).toBeTrue();

		// Third request - should get new result (different UUID)
		cfhttp(
			url="http://#variables.httpbin.server#:#variables.httpbin.port#/uuid",
			result="local.httpResult3",
			cachedwithin="#createTimeSpan(0,1,0,0)#"
		);
		var thirdResponse = local.httpResult3.filecontent;

		// Verify cache was flushed (different response)
		expect(thirdResponse).notToBe(firstResponse);
	}

	// ========================================
	// Query Tests
	// ========================================

	public void function testQueryCachedWithinId() localmode=true {
		if (!variables.hasDS) return;

		query datasource="#variables.datasourceName#" name="local.qTest" cachedwithin="#createTimeSpan(0,0,10,0)#" {
			writeOutput("SELECT RANDOM_UUID() AS uuid");
		}

		var cacheId = cachedWithinId(local.qTest);
		
		expect(cacheId).toBeString();
		expect(cacheId).notToBeEmpty();
	}

	public void function testQueryCachedWithinIdInResultMetadata() localmode=true {
		if (!variables.hasDS) return;

		query datasource="#variables.datasourceName#" name="local.qTest" cachedwithin="#createTimeSpan(0,0,10,0)#" result="local.r" {
			writeOutput("SELECT RANDOM_UUID() AS uuid");
		}

		expect(local.r).toHaveKey("cachedWithinId");
		expect(local.r.cachedWithinId).toBeString();
		expect(local.r.cachedWithinId).notToBeEmpty();
	}

	public void function testQueryCachedWithinFlush() localmode=true {
		if (!variables.hasDS) return;

		// First query - creates cache
		query datasource="#variables.datasourceName#" name="local.qTest1" cachedwithin="#createTimeSpan(0,1,0,0)#" {
			writeOutput("SELECT RANDOM_UUID() AS uuid");
		}
		var firstUuid = local.qTest1.uuid;

		// Second query - should return cached result (same UUID)
		query datasource="#variables.datasourceName#" name="local.qTest2" cachedwithin="#createTimeSpan(0,1,0,0)#" {
			writeOutput("SELECT RANDOM_UUID() AS uuid");
		}
		var secondUuid = local.qTest2.uuid;
		
		// Verify cache is working (same UUID)
		expect(firstUuid).toBe(secondUuid);

		// Flush the cache
		var flushResult = cachedWithinFlush(local.qTest1);
		expect(flushResult).toBeTrue();

		// Third query - should get new result (different UUID)
		query datasource="#variables.datasourceName#" name="local.qTest3" cachedwithin="#createTimeSpan(0,1,0,0)#" {
			writeOutput("SELECT RANDOM_UUID() AS uuid");
		}
		var thirdUuid = local.qTest3.uuid;

		// Verify cache was flushed (different UUID)
		expect(thirdUuid).notToBe(firstUuid);
	}

	public void function testQueryCachedWithinFlushUsingResultMetadata() localmode=true {
		if (!variables.hasDS) return;

		// Create cached query with result metadata
		query datasource="#variables.datasourceName#" name="local.qTest1" cachedwithin="#createTimeSpan(0,1,0,0)#" result="local.r1" {
			writeOutput("SELECT RANDOM_UUID() AS uuid");
		}
		var firstUuid = local.qTest1.uuid;

		// Verify cached
		query datasource="#variables.datasourceName#" name="local.qTest2" cachedwithin="#createTimeSpan(0,1,0,0)#" result="local.r2" {
			writeOutput("SELECT RANDOM_UUID() AS uuid");
		}
		expect(local.qTest2.uuid).toBe(firstUuid);

		// Flush using cacheRemove with result metadata
		cacheRemove(
			ids: local.r1.cachedWithinId,
			cacheName: cacheGetDefaultCacheName("query")
		);

		// Verify cache was flushed
		query datasource="#variables.datasourceName#" name="local.qTest3" cachedwithin="#createTimeSpan(0,1,0,0)#" {
			writeOutput("SELECT RANDOM_UUID() AS uuid");
		}
		expect(local.qTest3.uuid).notToBe(firstUuid);
	}

	// ========================================
	// Function Tests
	// ========================================

	public void function testFunctionCachedWithinIdWithArrayArgs() localmode=true {
		var result = getCachedValue(1);
		var cacheId = cachedWithinId(getCachedValue, [1]);
		
		expect(cacheId).toBeString();
		expect(cacheId).notToBeEmpty();
	}

	public void function testFunctionCachedWithinIdWithStructArgs() localmode=true {
		var result = getCachedValue(1);
		var cacheId = cachedWithinId(getCachedValue, {id: 1});
		
		expect(cacheId).toBeString();
		expect(cacheId).notToBeEmpty();
	}

	public void function testFunctionCachedWithinIdArrayAndStructEquivalent() localmode=true {
		var result = getCachedValue(1);
		var cacheIdArray = cachedWithinId(getCachedValue, [1]);
		var cacheIdStruct = cachedWithinId(getCachedValue, {id: 1});
		
		expect(cacheIdArray).toBe(cacheIdStruct);
	}

	public void function testFunctionCachedWithinFlushWithArrayArgs() localmode=true {
		// First call - creates cache
		var firstValue = getCachedValue(1);

		// Second call - should return cached result (same value)
		var secondValue = getCachedValue(1);
		expect(secondValue).toBe(firstValue);

		// Flush the cache for this specific argument
		var flushResult = cachedWithinFlush(getCachedValue, [1]);
		expect(flushResult).toBeTrue();

		// Third call - should get new result (different value)
		var thirdValue = getCachedValue(1);
		expect(thirdValue).notToBe(firstValue);
	}

	public void function testFunctionCachedWithinFlushWithStructArgs() localmode=true {
		// First call - creates cache
		var firstValue = getCachedValue(2);

		// Second call - should return cached result (same value)
		var secondValue = getCachedValue(2);
		expect(secondValue).toBe(firstValue);

		// Flush the cache using struct arguments
		var flushResult = cachedWithinFlush(getCachedValue, {id: 2});
		expect(flushResult).toBeTrue();

		// Third call - should get new result (different value)
		var thirdValue = getCachedValue(2);
		expect(thirdValue).notToBe(firstValue);
	}

	public void function testFunctionCachedWithinFlushSelectiveArguments() localmode=true {
		// Create cache entries for multiple arguments
		var value1 = getCachedValue(10);
		var value2 = getCachedValue(20);

		// Verify both are cached
		expect(getCachedValue(10)).toBe(value1);
		expect(getCachedValue(20)).toBe(value2);

		// Flush only argument 10
		cachedWithinFlush(getCachedValue, [10]);

		// Verify argument 10 was flushed but 20 remains cached
		expect(getCachedValue(10)).notToBe(value1);
		expect(getCachedValue(20)).toBe(value2);
	}

	public void function testFunctionCachedWithinFlushMultipleArgs() localmode=true {
		// First call - creates cache
		var firstValue = getMultiArgCachedValue(1, "test");

		// Second call - should return cached result
		var secondValue = getMultiArgCachedValue(1, "test");
		expect(secondValue).toBe(firstValue);

		// Flush with array arguments
		var flushResult = cachedWithinFlush(getMultiArgCachedValue, [1, "test"]);
		expect(flushResult).toBeTrue();

		// Third call - should get new result
		var thirdValue = getMultiArgCachedValue(1, "test");
		expect(thirdValue).notToBe(firstValue);
	}

	public void function testFunctionCachedWithinFlushMultipleArgsWithStruct() localmode=true {
		// First call - creates cache
		var firstValue = getMultiArgCachedValue(2, "demo");

		// Second call - should return cached result
		var secondValue = getMultiArgCachedValue(2, "demo");
		expect(secondValue).toBe(firstValue);

		// Flush with struct arguments
		var flushResult = cachedWithinFlush(
			getMultiArgCachedValue,
			{id: 2, name: "demo"}
		);
		expect(flushResult).toBeTrue();

		// Third call - should get new result
		var thirdValue = getMultiArgCachedValue(2, "demo");
		expect(thirdValue).notToBe(firstValue);
	}

	// ========================================
	// Error and Edge Case Tests
	// ========================================

	public void function testCachedWithinIdWithInvalidObjectThrows() localmode=true {
		expect( function() {
			cachedWithinId( "not a cache object" );
		}).toThrow();
	}

	public void function testCachedWithinFlushWithInvalidObjectThrows() localmode=true {
		expect( function() {
			cachedWithinFlush( "not a cache object" );
		}).toThrow();
	}

	public void function testCachedWithinFlushWithInvalidObjectErrorMessage() localmode=true {
		try {
			cachedWithinFlush( "not a cache object" );
			fail( "should have thrown" );
		}
		catch ( any e ) {
			expect( e.message ).toInclude( "CachedWithinFlush" );
		}
	}

	public void function testCachedWithinFlushAlreadyFlushedReturnsFalse() localmode=true {
		// Create a cache entry
		var firstValue = getCachedValue( 999 );

		// Flush it
		var first = cachedWithinFlush( getCachedValue, [999] );
		expect( first ).toBeTrue();

		// Flush again - entry no longer exists
		var second = cachedWithinFlush( getCachedValue, [999] );
		expect( second ).toBeFalse();
	}

	// ========================================
	// Helper Functions
	// ========================================

	private void function defineCaches() {
		// Configure test caches (separate cache for each type)
		var cache={};
		cache[variables.queryCacheName] = {
			class: 'lucee.runtime.cache.ram.RamCache',
			storage: false,
			default: "query",
			custom: {
				"timeToIdleSeconds": 86400,
				"timeToLiveSeconds": 86400
			}
		};
		cache[variables.functionCacheName] = {
			class: 'lucee.runtime.cache.ram.RamCache',
			storage: false,
			default: "function",
			custom: {
				"timeToIdleSeconds": 86400,
				"timeToLiveSeconds": 86400
			}
		};
		cache[variables.httpCacheName] = {
			class: 'lucee.runtime.cache.ram.RamCache',
			storage: false,
			default: "http",
			custom: {
				"timeToIdleSeconds": 86400,
				"timeToLiveSeconds": 86400
			}
		};
		
		application 
			action="update" 
			caches=cache;
	}

	private boolean function defineDatasource() localmode=true {
		var ds = getDatasource();
		if (structIsEmpty(ds)) return false;

		var datasources = {};
		datasources[variables.datasourceName] = ds;
		application action="update" datasources=datasources;
		return true;
	}

	private struct function getDatasource() localmode=true {
		// Try to get H2 datasource from server helper
		var data = server.getDatasource("h2", server._getTempDir("cachetest"));
		return data ?: {};
	}

	private function getCachedValue(id) cachedwithin="#createTimeSpan(0,1,0,0)#" {
		return createUUID();
	}

	private function getMultiArgCachedValue(id, name) cachedwithin="#createTimeSpan(0,1,0,0)#" {
		return createUUID() & "_" & id & "_" & name;
	}

}
</cfscript>
