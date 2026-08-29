component extends="org.lucee.cfml.test.LuceeTestCase" labels="udf" {

	function afterAll(){
		application action='update' nullSupport=false;
	}

	function run( testResults, testBox ) {

		describe( title='LDEV-5933 UDF Fast Path - functions qualifying for fast path (no types, no defaults, not required, pass-by-ref)', body=function(){

			describe( title='without full null support', body=function(){

				beforeEach( function( currentSpec, data ){
					application action='update' nullSupport=false;
				});

				it( title='fast path: all args provided', body=function() {
					var result = _fastPathUDF( "a", "b", "c" );
					expect( result.arg1 ).toBe( "a" );
					expect( result.arg2 ).toBe( "b" );
					expect( result.arg3 ).toBe( "c" );
				});

				it( title='fast path: fewer args than defined - missing args should be null', body=function() {
					var result = _fastPathUDF( "a" );
					expect( result.arg1 ).toBe( "a" );
					expect( isNull( result.arg2 ) ).toBeTrue();
					expect( isNull( result.arg3 ) ).toBeTrue();
				});

				it( title='fast path: no args provided - all should be null', body=function() {
					var result = _fastPathUDF();
					expect( isNull( result.arg1 ) ).toBeTrue();
					expect( isNull( result.arg2 ) ).toBeTrue();
					expect( isNull( result.arg3 ) ).toBeTrue();
				});

				it( title='fast path: more args than defined - extras should be accessible by index', body=function() {
					var result = _fastPathUDF( "a", "b", "c", "d", "e" );
					expect( result.arg1 ).toBe( "a" );
					expect( result.arg2 ).toBe( "b" );
					expect( result.arg3 ).toBe( "c" );
					expect( result[ 4 ] ).toBe( "d" );
					expect( result[ 5 ] ).toBe( "e" );
				});

				it( title='fast path: null value passed explicitly', body=function() {
					var result = _fastPathUDF( "a", javacast( "null", "" ), "c" );
					expect( result.arg1 ).toBe( "a" );
					expect( isNull( result.arg2 ) ).toBeTrue();
					expect( result.arg3 ).toBe( "c" );
				});

			});

			describe( title='with full null support', body=function(){

				beforeEach( function( currentSpec, data ){
					application action='update' nullSupport=true;
				});

				afterEach( function( currentSpec, data ){
					application action='update' nullSupport=false;
				});

				it( title='fast path FNS: all args provided', body=function() {
					var result = _fastPathUDF( "a", "b", "c" );
					expect( result.arg1 ).toBe( "a" );
					expect( result.arg2 ).toBe( "b" );
					expect( result.arg3 ).toBe( "c" );
				});

				it( title='fast path FNS: fewer args than defined - missing args should not exist', body=function() {
					var result = _fastPathUDF( "a" );
					expect( result.arg1 ).toBe( "a" );
					expect( result ).notToHaveKey( "arg2" );
					expect( result ).notToHaveKey( "arg3" );
				});

				it( title='fast path FNS: no args provided', body=function() {
					var result = _fastPathUDF();
					expect( result ).notToHaveKey( "arg1" );
					expect( result ).notToHaveKey( "arg2" );
					expect( result ).notToHaveKey( "arg3" );
				});

				it( title='fast path FNS: null value passed explicitly', body=function() {
					var result = _fastPathUDF( "a", null, "c" );
					expect( result.arg1 ).toBe( "a" );
					expect( result ).toHaveKey( "arg2" );
					expect( isNull( result.arg2 ) ).toBeTrue();
					expect( result.arg3 ).toBe( "c" );
				});

			});

		});

		describe( title='LDEV-5933 UDF Non-Fast Path - functions NOT qualifying (have types, defaults, required, or pass-by-value)', body=function(){

			beforeEach( function( currentSpec, data ){
				application action='update' nullSupport=false;
			});

			it( title='non-fast path: function with typed args', body=function() {
				var result = _typedArgsUDF( "hello", 42 );
				expect( result.str ).toBe( "hello" );
				expect( result.num ).toBe( 42 );
			});

			it( title='non-fast path: function with default values', body=function() {
				var result = _defaultArgsUDF( "provided" );
				expect( result.arg1 ).toBe( "provided" );
				expect( result.arg2 ).toBe( "default2" );
			});

			it( title='non-fast path: function with default values - no args', body=function() {
				var result = _defaultArgsUDF();
				expect( result.arg1 ).toBe( "default1" );
				expect( result.arg2 ).toBe( "default2" );
			});

			it( title='non-fast path: function with required arg', body=function() {
				var result = _requiredArgUDF( "required_value", "optional_value" );
				expect( result.req ).toBe( "required_value" );
				expect( result.opt ).toBe( "optional_value" );
			});

			it( title='non-fast path: required arg missing throws exception', body=function() {
				expect( function(){
					_requiredArgUDF();
				}).toThrow();
			});

		});

		describe( title='LDEV-5933 UDF argument behaviour consistency', body=function(){

			beforeEach( function( currentSpec, data ){
				application action='update' nullSupport=false;
			});

			it( title='fast path and non-fast path should behave identically with all args', body=function() {
				var fast = _fastPathUDF( "x", "y", "z" );
				var slow = _typedAnyUDF( "x", "y", "z" );
				expect( fast.arg1 ).toBe( slow.arg1 );
				expect( fast.arg2 ).toBe( slow.arg2 );
				expect( fast.arg3 ).toBe( slow.arg3 );
			});

			it( title='fast path and non-fast path should behave identically with partial args', body=function() {
				var fast = _fastPathUDF( "x" );
				var slow = _typedAnyUDF( "x" );
				expect( fast.arg1 ).toBe( slow.arg1 );
				expect( isNull( fast.arg2 ) ).toBe( isNull( slow.arg2 ) );
				expect( isNull( fast.arg3 ) ).toBe( isNull( slow.arg3 ) );
			});

			it( title='fast path and non-fast path should behave identically with no args', body=function() {
				var fast = _fastPathUDF();
				var slow = _typedAnyUDF();
				expect( isNull( fast.arg1 ) ).toBe( isNull( slow.arg1 ) );
				expect( isNull( fast.arg2 ) ).toBe( isNull( slow.arg2 ) );
				expect( isNull( fast.arg3 ) ).toBe( isNull( slow.arg3 ) );
			});

			it( title='fast path and non-fast path should behave identically with extra args', body=function() {
				var fast = _fastPathUDF( "a", "b", "c", "extra1", "extra2" );
				var slow = _typedAnyUDF( "a", "b", "c", "extra1", "extra2" );
				expect( fast[ 4 ] ).toBe( slow[ 4 ] );
				expect( fast[ 5 ] ).toBe( slow[ 5 ] );
			});

		});

	}

	// Fast path eligible: no type, no default, not required, pass-by-reference (default)
	private function _fastPathUDF( arg1, arg2, arg3 ){
		return arguments;
	}

	// NOT fast path: has explicit type (even though it's "any", the check is for CFTypes.TYPE_ANY constant)
	private function _typedAnyUDF( any arg1, any arg2, any arg3 ){
		return arguments;
	}

	// NOT fast path: has typed arguments
	private function _typedArgsUDF( string str, numeric num ){
		return arguments;
	}

	// NOT fast path: has default values
	private function _defaultArgsUDF( arg1="default1", arg2="default2" ){
		return arguments;
	}

	// NOT fast path: has required argument
	private function _requiredArgUDF( required req, opt ){
		return arguments;
	}

}
