component extends="org.lucee.cfml.test.LuceeTestCase" {

	function afterAll(){
		application action='update' nullSupport=false;
	};

	function run( testResults , testBox ) { 

		describe( title='check null argument handling with full null support' , body=function(){

			beforeEach( function( currentSpec, data ){
				application action='update' nullSupport=true;
			});

			afterEach( function( currentSpec, data ){
				application action='update' nullSupport=false;
			});

			it( title='full null support, no arguments, via argumentCollection', body=function() {
				var args = _proxy();
				expect( args.arg1 ).toBe( "default 1" );
				expect( args.arg2 ).toBe( "default 2" );
			});

			it( title='full null support, no arguments', body=function() {
				var args = _udf();
				expect( args.arg1 ).toBe( "default 1" );
				expect( args.arg2 ).toBe( "default 2" );
			});

			it( title='full null support, no arguments, without defaults', skip=true, body=function() {
				var args = _udfNoDefaults(); // args scope is currently empty
				expect( isNull( args.arg1 ) ).toBeTrue();
				expect( isNull( args.arg2 ) ).toBeTrue();
				expect( args ).toHaveKey( "arg1" );
				expect( args ).toHaveKey( "arg2" );
			});

		});

		describe( title='check null argument handling without full null support' , body=function(){

			beforeEach( function( currentSpec, data ){
				application action='update' nullSupport=false;
			});

			afterEach( function( currentSpec, data ){
				application action='update' nullSupport=false;
			});

			it( title='normal, no arguments, via argumentCollection', body=function() {
				var args = _proxy();
				expect( args.arg1 ).toBe( "default 1" );
				expect( args.arg2 ).toBe( "default 2" );
			});

			it( title='normal, no arguments, with defaults', body=function() {
				var args = _udf();
				expect( args.arg1 ).toBe( "default 1" );
				expect( args.arg2 ).toBe( "default 2" );
			});

			it( title='normal, no arguments, without defaults', body=function() {
				var args = _udfNoDefaults();
				expect( args ).notToHaveKey( "arg1" ); // keys with null values don't exist
				expect( args ).notToHaveKey( "arg2" ); // keys with null values don't exist
				expect( isNull( args.arg1 ) ).toBeTrue();
				expect( isNull( args.arg2 ) ).toBeTrue();
			});

		});

	}

	private function _proxy( arg1, arg2 ) {
		return _udf( argumentCollection = arguments );
	}
	private function _udf( arg1='default 1', arg2='default 2' ){
		return arguments;
	}

	private function _udfNoDefaults( arg1, arg2 ){
		return arguments;
	}
}