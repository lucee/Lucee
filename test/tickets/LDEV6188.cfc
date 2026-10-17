component extends="org.lucee.cfml.test.LuceeTestCase" {

	variables.functionCacheName = "ldev6188FunctionCache";

	function beforeAll() {
		// the cachedWithinFlush specs need a default function cache, CI has none configured
		try {
			variables.prevFunctionCache = cacheGetDefaultCacheName( "function" );
		}
		catch ( any e ) {
			variables.prevFunctionCache = "";
		}
		var caches = {};
		caches[ variables.functionCacheName ] = {
			class: "lucee.runtime.cache.ram.RamCache",
			storage: false,
			default: "function",
			custom: { "timeToIdleSeconds": 3600, "timeToLiveSeconds": 3600 }
		};
		application action="update" caches=caches;
	}

	function afterAll() {
		application action="update" nullSupport=false;
		try {
			cacheClear( cacheName=variables.functionCacheName );
		}
		catch ( any e ) {}
		if ( len( variables.prevFunctionCache ) ) {
			application action="update" caches={ "function": variables.prevFunctionCache };
		}
	}

	function run( testResults, testBox ) {

		describe( title="LDEV-6188 nullSupport=true: optional args should exist in arguments scope", body=function() {

			beforeEach( function( currentSpec, data ) {
				application action="update" nullSupport=true;
			});

			afterEach( function( currentSpec, data ) {
				application action="update" nullSupport=false;
			});

			it( title="positional call, no defaults — args should exist as null", body=function() {
				var args = _noDefaults();
				expect( args ).toHaveKey( "arg1" );
				expect( args ).toHaveKey( "arg2" );
				expect( isNull( args.arg1 ) ).toBeTrue();
				expect( isNull( args.arg2 ) ).toBeTrue();
			});

			it( title="positional call, typed args, no defaults — args should exist as null", body=function() {
				var args = _typedNoDefaults();
				expect( args ).toHaveKey( "memberId" );
				expect( args ).toHaveKey( "name" );
				expect( isNull( args.memberId ) ).toBeTrue();
				expect( isNull( args.name ) ).toBeTrue();
			});

			it( title="argumentCollection call, no defaults — args should exist as null", body=function() {
				var args = _proxyNoDefaults();
				expect( args ).toHaveKey( "arg1" );
				expect( args ).toHaveKey( "arg2" );
				expect( isNull( args.arg1 ) ).toBeTrue();
				expect( isNull( args.arg2 ) ).toBeTrue();
			});

			it( title="positional call, with defaults — args should use defaults", body=function() {
				var args = _withDefaults();
				expect( args ).toHaveKey( "arg1" );
				expect( args ).toHaveKey( "arg2" );
				expect( args.arg1 ).toBe( "default1" );
				expect( args.arg2 ).toBe( "default2" );
			});

			it( title="argumentCollection call, with defaults — args should use defaults", body=function() {
				var args = _proxyWithDefaults();
				expect( args ).toHaveKey( "arg1" );
				expect( args ).toHaveKey( "arg2" );
				expect( args.arg1 ).toBe( "default1" );
				expect( args.arg2 ).toBe( "default2" );
			});

			it( title="partial args passed — missing optional should exist as null", body=function() {
				var args = _noDefaults( "passed" );
				expect( args ).toHaveKey( "arg1" );
				expect( args ).toHaveKey( "arg2" );
				expect( args.arg1 ).toBe( "passed" );
				expect( isNull( args.arg2 ) ).toBeTrue();
			});

			it( title="partial args via argumentCollection — missing optional should exist as null", body=function() {
				var args = _proxyNoDefaults( "passed" );
				expect( args ).toHaveKey( "arg1" );
				expect( args ).toHaveKey( "arg2" );
				expect( args.arg1 ).toBe( "passed" );
				expect( isNull( args.arg2 ) ).toBeTrue();
			});

			it( title="explicit null positional — default is used (ACF compatible)", body=function() {
				var args = _withDefaults( nullValue(), "passed" );
				expect( args.arg1 ).toBe( "default1" );
				expect( args.arg2 ).toBe( "passed" );
			});

			it( title="explicit null named — default is used (ACF compatible)", body=function() {
				var args = _withDefaults( arg1=nullValue() );
				expect( args.arg1 ).toBe( "default1" );
				expect( args.arg2 ).toBe( "default2" );
			});

			it( title="argumentCollection with null value — default is used (ACF compatible)", body=function() {
				var args = _withDefaults( argumentCollection={ arg1: nullValue(), arg2: "passed" } );
				expect( args.arg1 ).toBe( "default1" );
				expect( args.arg2 ).toBe( "passed" );
			});

			it( title="argumentCollection proxy, two levels deep — defaults are kept", body=function() {
				var args = _proxyProxyWithDefaults();
				expect( args.arg1 ).toBe( "default1" );
				expect( args.arg2 ).toBe( "default2" );
			});

			it( title="argumentCollection proxy, partial args — missing arg gets default", body=function() {
				var args = _proxyWithDefaults( arg1="x" );
				expect( args.arg1 ).toBe( "x" );
				expect( args.arg2 ).toBe( "default2" );
			});

			it( title="explicit null without default — key exists as null", body=function() {
				var args = _noDefaults( nullValue(), "passed" );
				expect( args ).toHaveKey( "arg1" );
				expect( isNull( args.arg1 ) ).toBeTrue();
				expect( args.arg2 ).toBe( "passed" );
			});

			it( title="required arg accepts explicit null", body=function() {
				var args = _required( nullValue() );
				expect( args ).toHaveKey( "arg1" );
				expect( isNull( args.arg1 ) ).toBeTrue();
			});

			it( title="required arg not passed still throws", body=function() {
				expect( function() {
					_required();
				} ).toThrow();
			});

			it( title="cachedWithinFlush matches a call with unpassed optional args", body=function() {
				_testCachedWithinFlush();
			});

		});

		describe( title="LDEV-6188 nullSupport=false: optional args should not exist (control tests)", body=function() {

			beforeEach( function( currentSpec, data ) {
				application action="update" nullSupport=false;
			});

			it( title="positional call, no defaults — keys should not exist", body=function() {
				var args = _noDefaults();
				expect( args ).notToHaveKey( "arg1" );
				expect( args ).notToHaveKey( "arg2" );
			});

			it( title="positional call, with defaults — keys should exist with defaults", body=function() {
				var args = _withDefaults();
				expect( args ).toHaveKey( "arg1" );
				expect( args.arg1 ).toBe( "default1" );
			});

			it( title="cachedWithinFlush matches a call with unpassed optional args", body=function() {
				_testCachedWithinFlush();
			});

			// a null never counts as passed with null support off, so the default is used (unchanged behaviour)
			it( title="explicit null positional — default is used", body=function() {
				var args = _withDefaults( nullValue(), "passed" );
				expect( args.arg1 ).toBe( "default1" );
				expect( args.arg2 ).toBe( "passed" );
			});

			it( title="explicit null named — default is used", body=function() {
				var args = _withDefaults( arg1=nullValue() );
				expect( args.arg1 ).toBe( "default1" );
				expect( args.arg2 ).toBe( "default2" );
			});

			it( title="argumentCollection with null value — default is used", body=function() {
				var args = _withDefaults( argumentCollection={ arg1: nullValue(), arg2: "passed" } );
				expect( args.arg1 ).toBe( "default1" );
				expect( args.arg2 ).toBe( "passed" );
			});

			it( title="explicit null named, no default — key does not exist", body=function() {
				var args = _noDefaults( arg1=nullValue(), arg2="passed" );
				expect( args ).notToHaveKey( "arg1" );
				expect( isNull( args.arg1 ) ).toBeTrue();
				expect( args.arg2 ).toBe( "passed" );
			});

			it( title="argumentCollection proxy, no defaults — keys do not exist", body=function() {
				var args = _proxyNoDefaults();
				expect( args ).notToHaveKey( "arg1" );
				expect( args ).notToHaveKey( "arg2" );
			});

			it( title="argumentCollection proxy, with defaults — defaults are used", body=function() {
				var args = _proxyProxyWithDefaults( arg1="x" );
				expect( args.arg1 ).toBe( "x" );
				expect( args.arg2 ).toBe( "default2" );
			});

			it( title="required arg with explicit null throws (null counts as not passed)", body=function() {
				expect( function() {
					_required( nullValue() );
				} ).toThrow();
			});

		});

	}

	// helper functions

	private function _noDefaults( arg1, arg2 ) {
		return arguments;
	}

	private function _typedNoDefaults( Numeric memberId, String name ) {
		return arguments;
	}

	private function _withDefaults( arg1="default1", arg2="default2" ) {
		return arguments;
	}

	private function _proxyNoDefaults( arg1, arg2 ) {
		return _noDefaults( argumentCollection=arguments );
	}

	private function _proxyWithDefaults( arg1, arg2 ) {
		return _withDefaults( argumentCollection=arguments );
	}

	private function _proxyProxyWithDefaults( arg1, arg2 ) {
		return _proxyWithDefaults( argumentCollection=arguments );
	}

	private function _required( required arg1 ) {
		return arguments;
	}

	private function _cached( id, name ) cachedwithin="#createTimeSpan( 0, 1, 0, 0 )#" {
		return createUUID();
	}

	private function _testCachedWithinFlush() {
		var tag = createUUID();
		var first = _cached( tag );
		expect( _cached( tag ) ).toBe( first );

		expect( cachedWithinFlush( _cached, [ tag ] ) ).toBeTrue();
		var second = _cached( tag );
		expect( second ).notToBe( first );

		expect( cachedWithinFlush( _cached, { id: tag } ) ).toBeTrue();
		expect( _cached( tag ) ).notToBe( second );
	}

}
